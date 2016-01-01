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

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.annotation.Nonnull;

/**
 * Represents an event that is fired when a {@link BlurPlayer} causes damage to another {@link BlurPlayer}.
 */
public class PlayerDamagePlayerEvent extends BlurPlayerEvent implements Cancellable {

    private final BlurPlayer victim;
    private final EntityDamageByEntityEvent bukkitEvent;

    public PlayerDamagePlayerEvent(@Nonnull BlurPlayer killer, @Nonnull BlurPlayer victim, @Nonnull EntityDamageByEntityEvent bukkitEvent) {
        super(Preconditions.checkNotNull(killer, "killer cannot be null."));
        this.victim = Preconditions.checkNotNull(victim, "victim cannot be null.");
        this.bukkitEvent = Preconditions.checkNotNull(bukkitEvent, "bukkitEntity cannot be null.");
    }

    public BlurPlayer getVictim() {
        return victim;
    }

    public EntityDamageByEntityEvent getBukkitEvent() {
        return bukkitEvent;
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
