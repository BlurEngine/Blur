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

package com.blurengine.blur.framework;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.blurengine.blur.countdown.Countdown;
import com.blurengine.blur.framework.metadata.playerdata.PlayerAutoMetadataCreator;
import com.blurengine.blur.framework.metadata.teamdata.TeamAutoMetadataCreator;
import com.blurengine.blur.framework.ticking.TickFieldHolder;
import com.blurengine.blur.framework.ticking.TickMethodsCache;
import com.supaham.commons.bukkit.TickerTask;

import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents a component class that serves as a general class for using in modules. An example of a component is {@link Countdown}.
 */
public abstract class AbstractComponent implements Component {

    private final ModuleManager moduleManager;
    final Set<Listener> listeners = new HashSet<>();
    final Set<TickerTask> tasks = new HashSet<>();
    final Multimap<Object, TickerTask> tickableTasks = HashMultimap.create();
    final Set<TickerTask> tasksThatHaveBeenRan = new HashSet<>();
    private PlayerAutoMetadataCreator playerMetadataCreator;
    private TeamAutoMetadataCreator teamMetadataCreator;

    private ComponentState state = ComponentState.UNLOADED;

    public AbstractComponent(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        addListener(this);
        if (!(this instanceof TickFieldHolder)) {
            addTickable(this);
        }
    }

    public void load() {}

    public void unload() {}

    public void enable() {}

    public void disable() {}

    @Override
    public boolean tryLoad() {
        if (!setState(ComponentState.LOADED)) {
            return false;
        }
        this.tasksThatHaveBeenRan.clear();

        this.listeners.forEach(getSession().getBlur().getPlugin()::registerEvents);
        this.tasks.forEach(TickerTask::start);
        // TODO Fix further by modifying the TickerTask class to support pauses, etc. 
        // Identify all single-run tasks
        this.tasksThatHaveBeenRan.addAll(this.tasks.stream().filter(t -> t.getInterval() < 0).collect(Collectors.toList()));

        load();
        return true;
    }

    @Override
    public boolean tryUnload() {
        if (!setState(ComponentState.UNLOADED)) {
            return false;
        }
        this.listeners.forEach(getSession().getBlur().getPlugin()::unregisterEvents);
        this.tasks.forEach(TickerTask::stop);
        this.tasksThatHaveBeenRan.clear();

        unload();
        return true;
    }

    @Override
    public boolean tryEnable() {
        if (!setState(ComponentState.ENABLED)) {
            return false;
        }

        enable();
        return true;
    }

    @Override
    public boolean tryDisable() {
        if (!setState(ComponentState.LOADED)) {
            return false;
        }

        disable();
        return true;
    }

    @Nonnull
    @Override
    public ComponentState getState() {
        return state;
    }

    private boolean setState(ComponentState state) {
        if (this.state.isAcceptable(state)) {
            this.state = state;
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Nonnull
    @Override
    public Set<Listener> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }

    @Override
    public boolean hasListener(@Nonnull Listener listener) {
        return this.listeners.contains(Preconditions.checkNotNull(listener, "listener cannot be null."));
    }

    @Override
    public boolean addListener(@Nonnull Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null.");
        if (this.listeners.add(listener)) {
            if (this.state != ComponentState.UNLOADED) {
                getSession().getBlur().getPlugin().registerEvents(listener);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeListener(@Nonnull Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null.");
        if (this.listeners.remove(listener)) {
            if (this.state != ComponentState.UNLOADED) {
                getSession().getBlur().getPlugin().unregisterEvents(listener);
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public Collection<Object> getTickables() {
        return Collections.unmodifiableCollection(this.tickableTasks.keySet());
    }

    @Override
    public boolean hasTickable(@Nonnull Object tickable) {
        return this.tickableTasks.containsKey(Preconditions.checkNotNull(tickable, "tickable cannot be null."));
    }

    @Override
    public boolean addTickable(@Nonnull Object tickable) {
        Preconditions.checkNotNull(tickable, "tickable cannot be null.");
        if (!this.tickableTasks.containsKey(tickable)) {
            TickMethodsCache.loadTickableReturnTaskBuilders(getSession().getTicksPerSecond(), tickable).forEach(t -> {
                TickerTask task = t.plugin(getSession().getBlur().getPlugin()).build();
                addTask(task);
                tickableTasks.put(tickable, task);
            });
            getModuleManager().getTickFieldHolder().load(tickable);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTickable(@Nonnull Object tickable) {
        Preconditions.checkNotNull(tickable, "tickable cannot be null.");
        Collection<TickerTask> tasks = this.tickableTasks.removeAll(tickable);
        tasks.forEach(this::removeTask);
        return tasks.size() > 0;
    }

    @Nonnull
    @Override
    public Set<TickerTask> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    @Override
    public boolean hasTask(@Nonnull TickerTask task) {
        Preconditions.checkNotNull(task, "task cannot be null.");
        return this.tasks.contains(task);
    }

    @Override
    public boolean addTask(@Nonnull TickerTask task) {
        if (this.tasks.add(task)) {
            if (this.state != ComponentState.UNLOADED) {
                task.start();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTask(@Nonnull TickerTask task) {
        Preconditions.checkNotNull(task, "task cannot be null.");
        if (this.tasks.remove(task)) {
            // Always attempt to stop a task when removing it, no errors are thrown.
            task.stop();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public PlayerAutoMetadataCreator getPlayerMetadataCreator() {
        if (playerMetadataCreator == null) {
            playerMetadataCreator = new PlayerAutoMetadataCreator();
        }
        return playerMetadataCreator;
    }

    @Nonnull
    @Override
    public TeamAutoMetadataCreator getTeamMetadataCreator() {
        if (teamMetadataCreator == null) {
            teamMetadataCreator = new TeamAutoMetadataCreator(getTeamManager());
        }
        return teamMetadataCreator;
    }
}
