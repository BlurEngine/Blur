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

import com.blurengine.blur.events.session.BlurSessionEvent;
import com.blurengine.blur.framework.ticking.TickFieldHolder;
import com.blurengine.blur.modules.extents.ExtentManager;
import com.blurengine.blur.modules.filters.FilterManager;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.modules.teams.TeamManager;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import pluginbase.logging.PluginLogger;

/**
 * Represents a helper interface for use with the Blur framework. This interface contains a bunch of methods both identified as delegates and 
 * utilities that are used throughout the project, most commonly via {@link Component}, thus {@link Module}.
 */
public interface SessionHelperInterface {

    @Nonnull
    BlurSession getSession();

    @Nonnull
    ModuleManager getModuleManager();

    /* ================================
     * >> UTILITY METHODS
     * ================================ */

    default boolean isSession(@Nonnull PlayerEvent playerEvent) {
        Preconditions.checkNotNull(playerEvent, "playerEvent cannot be null.");
        return isSession(getPlayer(playerEvent.getPlayer()).getSession());
    }

    default boolean isSession(@Nonnull BlurSessionEvent sessionEvent) {
        Preconditions.checkNotNull(sessionEvent, "sessionEvent cannot be null.");
        return isSession(sessionEvent.getSession());
    }

    default boolean isSession(@Nonnull BlurSession session) {
        Preconditions.checkNotNull(session, "session cannot be null.");
        return getSession().equals(session);
    }
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    default void broadcastMessage(@Nonnull String message, Object... args) {
        Preconditions.checkNotNull(message, "message cannot be null.");
        getSession().broadcastMessage(message, args);
    }

    default void broadcastMessage(@Nonnull BaseComponent component) {
        getSession().broadcastMessage(Preconditions.checkNotNull(component, "component cannot be null."));
    }

    default void broadcastMessage(@Nonnull BaseComponent[] componentList) {
        BaseComponent component = new TextComponent(componentList);
        getSession().broadcastMessage(Preconditions.checkNotNull(component, "component cannot be null."));
    }

    default Collection<BlurPlayer> getPlayers() {
        return getSession().getPlayers().values();
    }

    @Nonnull
    default List<BlurPlayer> getPlayers(@Nonnull Predicate<BlurPlayer> predicate) {
        Preconditions.checkNotNull(predicate, "predicate cannot be null.");
        return getSession().getPlayers(predicate);
    }

    @Nonnull
    default Optional<BlurPlayer> getRandomPlayer(@Nonnull Predicate<BlurPlayer> predicate) {
        return getSession().getRandomPlayer(predicate);
    }

    @Nonnull
    default Stream<BlurPlayer> getPlayersStream() {
        return getSession().getPlayersStream();
    }

    @Nonnull
    default BlurPlayer getPlayer(@Nonnull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null.");
        return getSession().getPlayer(player);
    }

    @Nonnull
    default Optional<BlurPlayer> getPlayer(@Nonnull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null.");
        return getSession().getPlayer(uuid);
    }

    @Nonnull
    default PluginLogger getLogger() {
        return getSession().getLogger();
    }

    default TickFieldHolder getTickFieldHolder() {
        return getModuleManager().getTickFieldHolder();
    }

    default FilterManager getFilterManager() {
        return getModuleManager().getFilterManager();
    }

    default ExtentManager getExtentManager() {
        return getModuleManager().getExtentManager();
    }

    default TeamManager getTeamManager() {
        return getModuleManager().getTeamManager();
    }

    default StageManager getStageManager() {
        return getModuleManager().getStageManager();
    }
}
