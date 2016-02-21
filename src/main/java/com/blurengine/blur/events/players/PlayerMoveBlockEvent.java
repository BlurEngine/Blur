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

package com.blurengine.blur.events.players;

import com.google.common.base.Preconditions;

import com.blurengine.blur.session.BlurPlayer;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.annotation.Nonnull;

/**
 * Represents an event that is called when a {@link BlurPlayer} moves to a different block. This is different as opposed to {@link PlayerMoveEvent}
 * which is called whenever a player moves.
 */
public class PlayerMoveBlockEvent extends BlurPlayerEvent implements Cancellable {

    private final PlayerMoveEvent bukkitEvent;

    public PlayerMoveBlockEvent(@Nonnull PlayerMoveEvent bukkitEvent, @Nonnull BlurPlayer blurPlayer) {
        super(Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null."), blurPlayer.getSession());
        this.bukkitEvent = Preconditions.checkNotNull(bukkitEvent, "bukkitEvent cannot be null.");
    }

    public Location getFrom() {
        return bukkitEvent.getFrom();
    }

    public Location getTo() {
        return bukkitEvent.getTo();
    }

    public void setTo(Location to) {
        bukkitEvent.setTo(to);
    }

    @Override
    public boolean isCancelled() { return bukkitEvent.isCancelled(); }

    @Override
    public void setCancelled(boolean cancelled) { bukkitEvent.setCancelled(cancelled); }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
