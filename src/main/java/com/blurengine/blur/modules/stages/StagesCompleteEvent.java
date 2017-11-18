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

/**
 * Called when a {@link StageManager} reaches the end of its stages according to {@link StageManager#nextStage(StageChangeReason)}. Keep in mind
 * {@link StageChangedEvent} will still be called.
 *
 * @see StageChangedEvent
 */
public class StagesCompleteEvent extends StageEvent {

    private final StageChangeData changeData;
    private final Stage lastStage;

    public StagesCompleteEvent(@Nonnull StageChangeData changeData, @Nonnull Stage lastStage) {
        super(Preconditions.checkNotNull(lastStage, "lastStage cannot be null.").getManager());
        this.changeData = Preconditions.checkNotNull(changeData, "changeData cannot be null.");
        this.lastStage = lastStage;
    }

    public StageChangeData getChangeData() {
        return changeData;
    }

    public Stage getLastStage() {
        return lastStage;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
