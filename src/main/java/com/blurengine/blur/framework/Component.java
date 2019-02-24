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

import com.blurengine.blur.countdown.Countdown;
import com.blurengine.blur.framework.metadata.playerdata.PlayerAutoMetadataCreator;
import com.blurengine.blur.framework.metadata.teamdata.TeamAutoMetadataCreator;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.utils.TaskBuilder;
import com.supaham.commons.bukkit.TickerTask;

import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import co.aikar.commands.BaseCommand;

/**
 * Represents a component class that serves as a general class for using in modules. An example of a component is {@link Countdown}.
 */
public interface Component extends Listener, SessionHelperInterface {

    /**
     * Called when this module is being loaded, which is when the session is being loaded. At that point in time, only the map has been
     * loaded, and players are not setup. This shouldn't be confused with {@link #tryEnable()}, which is called when the session starts.
     * <p />
     * <b>Note: This component is automatically registered as a listener.</b> <br />
     * <b>Note: This component is automatically registered as a tickable.</b>
     */
    boolean tryLoad();

    /**
     * Called when this module is being unloaded, which is when the session is being unloaded. At that point in time, the map is still loaded, but
     * the players are no longer in the session.
     * <b>All registered listeners and tasks are stopped in this method. </b>
     */
    boolean tryUnload();

    /**
     * Called when this module is being enabled, which is when the session is being enabled. At that point in time, this module has been loaded and
     * will immediately begin to setup online players.
     */
    boolean tryEnable();

    /**
     * Called when this module is being enabled, which is when the session is being disabled. At that point in time, this module still has not been
     * unloaded.
     */
    boolean tryDisable();

    @Nonnull
    ComponentState getState();

    @Nonnull
    ModuleManager getModuleManager();

    /**
     * Returns an immutable set of {@link Listener}s registered to this session.
     *
     * @return set of listeners
     */
    @Nonnull
    Set<Listener> getListeners();

    /**
     * Returns whether a {@link Listener} is registered to this session.
     *
     * @param listener listener to check
     *
     * @return whether the listener is added to this session
     */
    boolean hasListener(@Nonnull Listener listener);

    /**
     * Registers a {@link Listener} instance to this session.
     * <p />
     * <b>Note: This module is automatically registered as a listener.</b>
     *
     * @param listener listener instance to register
     *
     * @return whether this set did not already contain the specified element
     */
    boolean addListener(@Nonnull Listener listener);

    /**
     * Unregisters a {@link Listener} from this session.
     *
     * @param listener listener to unregister
     *
     * @return whether this set contained the specified element
     */
    boolean removeListener(@Nonnull Listener listener);

    @Nonnull
    Collection<Object> getTickables();

    /**
     * Returns whether a tickable task is registered to this module.
     *
     * @param tickable tickable to check
     *
     * @return whether the task is added to this session
     *
     * @see #hasTask(TickerTask)
     */
    boolean hasTickable(@Nonnull Object tickable);

    /**
     * Registers a Tickable object to this module. Upon registration, if this module is enabled, the task will begin ticking starting the next
     * tick until it is unregistered using {@link #removeTask(TickerTask)}.
     * <p />
     * <b>Note: If this module implements Tickable, it will automatically register as a task.</b>
     *
     * @param tickable tickable to register
     *
     * @return whether the task was added to this module
     *
     * @see #addTask(TickerTask)
     */
    boolean addTickable(@Nonnull Object tickable);

    /**
     * Unregisters a Tickable from this module.
     *
     * @param tickable tickable to unregister
     *
     * @return whether the tickable was removed from this module
     *
     * @see #removeTask(TickerTask)
     */
    boolean removeTickable(@Nonnull Object tickable);

    /**
     * Returns an immutable set of Tickables registered to this module.
     *
     * @return set of tickables
     */
    @Nonnull
    Set<TickerTask> getTasks();

    /**
     * Returns whether a task is registered to this module.
     *
     * @param task task to check
     *
     * @return whether the task is added to this session
     */
    boolean hasTask(@Nonnull TickerTask task);

    /**
     * Registers a {@link TickerTask} to this module. Upon registration, if this module is enabled, the task will begin ticking starting the next
     * tick until it is unregistered using {@link #removeTask(TickerTask)}.
     * <p />
     * <b>Note: If this module implements Tickable, it will automatically register as a task.</b>
     *
     * @param task task to register
     *
     * @return whether the task was added to this module
     */
    boolean addTask(@Nonnull TickerTask task);

    /**
     * Unregisters a {@link TickerTask} from this module.
     *
     * @param task task to unregister
     *
     * @return whether the task was removed from this module
     */
    boolean removeTask(@Nonnull TickerTask task);

    @Nonnull
    default TaskBuilder newTask(@Nullable Runnable runnable) {
        return newTask().run(runnable);
    }

    @Nonnull
    default TaskBuilder newUnregisteredTask(@Nullable Runnable runnable) {
        return newTask().unregistered().run(runnable);
    }

    @Nonnull
    default TaskBuilder newTask() {
        return new TaskBuilder(getSession().getBlur().getPlugin()) {
            @Override
            public TickerTask build() {
                TickerTask task = super.build();
                if (isUnregistered()) {
                    task.start();
                } else {
                    addTask(task);
                }
                return task;
            }
        };
    }

    @Override
    @Nonnull
    default BlurSession getSession() {
        return getModuleManager().getSession();
    }

    @Nonnull
    PlayerAutoMetadataCreator getPlayerMetadataCreator();

    @Nonnull
    TeamAutoMetadataCreator getTeamMetadataCreator();

    @Nonnull
    Collection<Component> getSubcomponents();

    boolean addSubcomponent(@Nonnull Component component);

    boolean removeSubcomponent(@Nonnull Component component);

    /**
     * Returns an immutable set of commands registered to this component.
     *
     * @return set of tickables
     */
    @Nonnull
    Set<BaseCommand> getCommands();

    /**
     * Returns whether a {@link BaseCommand} is registered to this component.
     *
     * @param command command to check
     *
     * @return whether the command is added to this component
     */
    boolean hasCommand(@Nonnull BaseCommand command);

    /**
     * Registers a {@link BaseCommand} to this component. Upon registration, if this module is enabled, the command will automatically be
     * registered into the server until it is unregistered using {@link #removeCommand(BaseCommand)}.
     *
     * @param command command to register
     *
     * @return whether the command was added to this component
     */
    boolean addCommand(@Nonnull BaseCommand command);

    /**
     * Unregisters a {@link BaseCommand} from this component.
     *
     * @param command command to unregister
     *
     * @return whether the command was removed from this component
     */
    boolean removeCommand(@Nonnull BaseCommand command);
}
