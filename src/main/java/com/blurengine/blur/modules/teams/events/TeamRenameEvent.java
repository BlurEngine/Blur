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

import com.blurengine.blur.modules.teams.BlurTeam;

import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a {@link BlurTeam}'s name changes.
 */
public class TeamRenameEvent extends TeamEvent {

    private String oldName;
    private String newName;

    public TeamRenameEvent(@Nonnull BlurTeam team, @Nonnull String oldName, @Nullable String newName) {
        super(team);
        this.oldName = Preconditions.checkNotNull(oldName, "oldName cannot be null.");
        this.newName = Preconditions.checkNotNull(newName, "newName cannot be null.");
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
