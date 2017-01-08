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
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Represents an event that is fired when a {@link BlurPlayer} kills another {@link BlurPlayer}.
 */
public class BlurPlayerDeathEvent extends BlurPlayerEvent {

    private final Location location;

    public BlurPlayerDeathEvent(@Nonnull BlurPlayer victim, @Nonnull Location location) {
        super(Preconditions.checkNotNull(victim, "victim cannot be null."), victim.getSession());
        this.location = Preconditions.checkNotNull(location, "location cannot be null.");
    }

    public Location getLocation() {
        return location;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
