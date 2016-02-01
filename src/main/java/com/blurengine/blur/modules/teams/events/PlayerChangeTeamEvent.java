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

package com.blurengine.blur.modules.teams.events;

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.players.BlurPlayerEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.modules.teams.SessionTeam;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Represents a {@link BlurPlayerEvent} where a player's {@link SessionTeam} has changed.
 */
public class PlayerChangeTeamEvent extends BlurPlayerEvent implements Cancellable {

    private final SessionTeam oldTeam;
    private SessionTeam newTeam;

    public PlayerChangeTeamEvent(@Nonnull BlurPlayer blurPlayer, @Nonnull SessionTeam oldTeam, @Nonnull SessionTeam newTeam) {
        super(Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null."), blurPlayer.getSession());
        this.oldTeam = Preconditions.checkNotNull(oldTeam, "oldTeam cannot be null.");
        this.newTeam = Preconditions.checkNotNull(newTeam, "newTeam cannot be null.");
    }

    public SessionTeam getOldTeam() {
        return oldTeam;
    }

    public SessionTeam getNewTeam() {
        return newTeam;
    }

    public void setNewTeam(SessionTeam newTeam) {
        this.newTeam = newTeam;
    }

    private boolean cancelled;

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
