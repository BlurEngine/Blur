/*
 * Copyright 2016 Ali Moghnieh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blurengine.blur.modules.framework;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.events.session.BlurSessionEvent;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.utils.TaskBuilder;
import com.blurengine.blur.session.Tickable;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.text.FancyMessage;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.NonNull;
import pluginbase.logging.PluginLogger;

/**
 * Module framework base class. Upon extending this class, you will need to annotate the extension with {@link ModuleInfo} to tell the framework
 * how to load this module from the config file. Immediately after implementation, ensure that the class is also registered to the
 * {@link ModuleManager} using {@link ModuleLoader#register(Class)}.
 *
 * @see #Module(ModuleManager)
 */
public abstract class Module implements Listener {

    protected final ModuleManager moduleManager;
    private final ModuleInfo moduleInfo;
    private final Set<Listener> listeners = new HashSet<>();
    private final Set<TickerTask> tasks = new HashSet<>();
    private final Multimap<Tickable, TickerTask> tickableTasks = HashMultimap.create();
    private final List<Module> submodules = new ArrayList<>();
    private ModuleState state = ModuleState.UNLOADED;

    public Module(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        this.moduleInfo = ModuleLoader.getModuleInfoByModule(getClass());
        init();
    }

    private void init() {
        addListener(this);
        if (this instanceof Tickable) {
            addTickable(((Tickable) this));
        }
    }

    /**
     * Called when this module is being loaded, which is when the session is being loaded. At that point in time, only the map has been
     * loaded, and players are not setup. This shouldn't be confused with {@link #enable()}, which is called when the session starts.
     * <p />
     * <b>Note: This module is automatically registered as a listener.</b> <br />
     * <b>Note: If this module implements {@link Tickable}, this object is automatically registered as a task.</b>
     */
    public void load() {
        this.state = ModuleState.LOADED;
        this.listeners.forEach(getSession().getBlur().getPlugin()::registerEvents);
        this.tasks.forEach(TickerTask::start);
        this.submodules.forEach(moduleManager::loadModule);
    }

    /**
     * Called when this module is being unloaded, which is when the session is being unloaded. At that point in time, the map is still loaded, but
     * the players are no longer in the session.
     * <b>All registered listeners and tasks are stopped in this method. </b>
     */
    public void unload() {
        this.state = ModuleState.UNLOADED;
        this.listeners.forEach(getSession().getBlur().getPlugin()::unregisterEvents);
        this.tasks.forEach(TickerTask::stop);
        this.submodules.forEach(moduleManager::unloadModule);
    }

    /**
     * Called when this module is being enabled, which is when the session is being enabled. At that point in time, this module has been loaded and
     * will immediately begin to setup online players.
     */
    public void enable() {
        this.state = ModuleState.ENABLED;
        this.submodules.forEach(moduleManager::enableModule);
    }

    /**
     * Called when this module is being enabled, which is when the session is being disabled. At that point in time, this module still has not been
     * unloaded.
     */
    public void disable() {
        this.state = ModuleState.LOADED;
        this.submodules.forEach(moduleManager::disableModule);
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    /**
     * Returns an immutable set of {@link Listener}s registered to this session.
     *
     * @return set of listeners
     */
    public Set<Listener> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }

    /**
     * Returns whether a {@link Listener} is registered to this session.
     *
     * @param listener listener to check
     *
     * @return whether the listener is added to this session
     */
    public boolean hasListener(@NonNull Listener listener) {
        return this.listeners.contains(listener);
    }

    /**
     * Registers a {@link Listener} instance to this session.
     * <p />
     * <b>Note: This module is automatically registered as a listener.</b>
     *
     * @param listener listener instance to register
     *
     * @return whether this set did not already contain the specified element
     */
    public boolean addListener(@NonNull Listener listener) {
        if (this.listeners.add(listener)) {
            if (this.state != ModuleState.UNLOADED) {
                getSession().getBlur().getPlugin().registerEvents(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Unregisters a {@link Listener} from this session.
     *
     * @param listener listener to unregister
     *
     * @return whether this set contained the specified element
     */
    public boolean removeListener(@NonNull Listener listener) {
        if (this.listeners.remove(listener)) {
            if (this.state != ModuleState.UNLOADED) {
                getSession().getBlur().getPlugin().unregisterEvents(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns whether a tickable task is registered to this module.
     *
     * @param tickable tickable to check
     *
     * @return whether the task is added to this session
     *
     * @see #hasTask(TickerTask)
     */
    public boolean hasTickable(@NonNull Tickable tickable) {
        return this.tickableTasks.containsKey(tickable);
    }

    /**
     * Registers a {@link Tickable} to this module. Upon registration, if this module is enabled, the task will begin ticking starting the next
     * tick until it is unregistered using {@link #removeTask(TickerTask)}.
     * <p />
     * <b>Note: If this module implements {@link Tickable}, it will automatically register as a task.</b>
     *
     * @param tickable tickable to register
     *
     * @return whether the task was added to this module
     *
     * @see #addTask(TickerTask)
     */
    public boolean addTickable(@NonNull Tickable tickable) {
        if (!this.tickableTasks.containsKey(tickable)) {
            TickMethodsCache.loadTickableReturnTaskBuilders(tickable).forEach(t -> {
                TickerTask task = t.plugin(this.getSession().getBlur().getPlugin()).build();
                addTask(task);
                tickableTasks.put(tickable, task);
            });
            return true;
        }
        return false;
    }

    /**
     * Unregisters a {@link Tickable} from this module.
     *
     * @param tickable tickable to unregister
     *
     * @return whether the tickable was removed from this module
     *
     * @see #removeTask(TickerTask)
     */
    public boolean removeTickable(@NonNull Tickable tickable) {
        Collection<TickerTask> tasks = this.tickableTasks.removeAll(tickable);
        tasks.forEach(this::removeTask);
        return tasks.size() > 0;
    }

    /**
     * Returns an immutable set of {@link Tickable}s registered to this module.
     *
     * @return set of tickables
     */
    public Set<TickerTask> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    /**
     * Returns whether a task is registered to this module.
     *
     * @param task task to check
     *
     * @return whether the task is added to this session
     */
    public boolean hasTask(@NonNull TickerTask task) {
        return this.tasks.contains(task);
    }

    /**
     * Registers a {@link TickerTask} to this module. Upon registration, if this module is enabled, the task will begin ticking starting the next
     * tick until it is unregistered using {@link #removeTask(TickerTask)}.
     * <p />
     * <b>Note: If this module implements {@link Tickable}, it will automatically register as a task.</b>
     *
     * @param task task to register
     *
     * @return whether the task was added to this module
     */
    public boolean addTask(@NonNull TickerTask task) {
        if (this.tasks.add(task)) {
            if (this.state != ModuleState.UNLOADED) {
                task.start();
            }
            return true;
        }
        return false;
    }

    /**
     * Unregisters a {@link TickerTask} from this module.
     *
     * @param task task to unregister
     *
     * @return whether the task was removed from this module
     */
    public boolean removeTask(@NonNull TickerTask task) {
        if (this.tasks.remove(task)) {
            if (this.state != ModuleState.UNLOADED) {
                task.stop();
            }
            return true;
        }
        return false;
    }

    public TaskBuilder newTask(Runnable runnable) {
        return newTask().run(runnable);
    }

    public TaskBuilder newTask() {
        return new TaskBuilder(this.getSession().getBlur().getPlugin()) {
            @Override
            public TickerTask build() {
                TickerTask task = super.build();
                if (task.getInterval() >= 0) {
                    addTask(task);
                }
                return task;
            }
        };
    }

    protected boolean addSubmodule(@NonNull Module module) {
        if (this.submodules.add(module)) {
            if (this.state == ModuleState.UNLOADED) {
                return true;  // Added successfully without loading.
            }

            if (moduleManager.loadModule(module)) {
                if (this.state == ModuleState.ENABLED) {
                    if (moduleManager.enableModule(module)) {
                        return true; // Added, loaded, and enabled successfully.
                    } else {
                        return false; // Failed to enable
                    }
                }
                return true; // Added and loaded successfully.
            } else {
                return false; // Failed to load
            }
        }
        return false;
    }

    public boolean removeSubmodule(@NonNull Module module) {
        if (this.submodules.remove(module)) {
            moduleManager.disableModule(module);
            moduleManager.unloadModule(module);
            return true;
        }
        return false;
    }

    public Collection<Module> getSubmodules() {
        return Collections.unmodifiableCollection(submodules);
    }

    public ModuleState getState() {
        return state;
    }
    
    /* ================================
     * >> UTILITY METHODS
     * ================================ */

    public boolean isSession(BlurSessionEvent sessionEvent) {
        return isSession(sessionEvent.getBlurSession());
    }

    public boolean isSession(BlurSession session) {
        return getSession().equals(session);
    }
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public void broadcastMessage(@NonNull String message, Object... args) {
        getSession().broadcastMessage(message, args);
    }

    public void broadcastMessage(@NonNull FancyMessage fancyMessage) {
        getSession().broadcastMessage(fancyMessage);
    }

    public Set<BlurPlayer> getPlayers(Predicate<BlurPlayer> predicate) {
        return getSession().getPlayers(predicate);
    }

    public Optional<BlurPlayer> getAnyPlayer(Predicate<BlurPlayer> predicate) {
        return getSession().getAnyPlayer(predicate);
    }

    public Stream<BlurPlayer> getPlayersStream() {
        return getSession().getPlayersStream();
    }

    public BlurPlayer getPlayer(Player player) {
        return getSession().getPlayer(player);
    }

    public BlurPlayer getPlayer(UUID uuid) {
        return getSession().getPlayer(uuid);
    }

    public BlurSession getSession() {
        return moduleManager.getSession();
    }

    public PluginLogger getLogger() {
        return getSession().getLogger();
    }

    public StageManager getStagesManager() {
        return moduleManager.getStageManager();
    }
}
