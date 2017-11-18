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

package com.blurengine.blur.modules.stages;

import com.google.common.base.Preconditions;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreStageChangeEvent extends StageEvent implements Cancellable {

    private final Stage newStage;
    private final StageChangeData changeData;

    public PreStageChangeEvent(@Nonnull StageManager manager, @Nonnull StageChangeData changeData, @Nullable Stage newStage) {
        super(Preconditions.checkNotNull(manager, "manager cannot be null."));
        this.changeData = Preconditions.checkNotNull(changeData, "reason cannot be null.");
        this.newStage = newStage;
        if (this.newStage != null) {
            Preconditions.checkArgument(manager.equals(newStage.getManager()), "newStage manager is not the same as the given manager.");
        }
    }

    public StageChangeData getChangeData() {
        return changeData;
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
