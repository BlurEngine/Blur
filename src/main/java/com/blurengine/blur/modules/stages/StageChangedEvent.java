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

import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a {@link StageManager} has changed its current stage. Keep in mind, when all stages have been completed {@link StagesCompleteEvent}
 * will be called alongside this event. This is done for user convenience so please use it where appropriate.
 *
 * @see StagesCompleteEvent
 */
public class StageChangedEvent extends StageEvent {

    private final Stage newStage;
    private final Stage oldStage;
    private final StageChangeData changeData;

    public StageChangedEvent(@Nonnull StageChangeData changeData, @Nonnull Stage newStage, @Nullable Stage oldStage) {
        super(Preconditions.checkNotNull(newStage, "newStage cannot be null.").getManager());
        this.changeData = Preconditions.checkNotNull(changeData, "reason cannot be null.");
        this.newStage = newStage;
        this.oldStage = oldStage;
        if (this.oldStage != null) {
            Preconditions.checkArgument(this.oldStage.getManager().equals(newStage.getManager()),
                "newStage manager is not the same as the oldStage manager.");
        }
    }

    @Nonnull
    public Stage getNewStage() {
        return newStage;
    }

    @Nullable
    public Stage getOldStage() {
        return oldStage;
    }

    @Nonnull
    public StageChangeData getChangeData() {
        return changeData;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
