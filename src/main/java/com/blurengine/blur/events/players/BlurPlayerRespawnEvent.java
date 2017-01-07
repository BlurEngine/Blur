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

import com.blurengine.blur.modules.spawns.Spawn;
import com.blurengine.blur.session.BlurPlayer;

import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an event that is fired when a {@link BlurPlayer} is to respawn in the session. 
 */
public class BlurPlayerRespawnEvent extends BlurPlayerEvent {

    private Spawn spawn;

    public BlurPlayerRespawnEvent(@Nonnull BlurPlayer player) {
        this(player, null);
    }

    public BlurPlayerRespawnEvent(@Nonnull BlurPlayer player, @Nullable Spawn spawn) {
        super(Preconditions.checkNotNull(player, "player cannot be null."), player.getSession());
        this.spawn = spawn;
    }

    public Spawn getSpawn() {
        return spawn;
    }

    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
