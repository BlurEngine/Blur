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

package com.blurengine.blur.modules.maploading;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.io.File;

public class ChooseNextMapEvent extends Event {

    private final MapLoaderModule mapLoader;
    private BlurMap nextMap;

    public ChooseNextMapEvent(MapLoaderModule mapLoader, BlurMap nextMap) {
        this.mapLoader = mapLoader;
        this.nextMap = nextMap;
    }

    public MapLoaderModule getMapLoader() {
        return mapLoader;
    }

    public BlurMap getNextMap() {
        return nextMap;
    }

    public void setNextMap(BlurMap nextMap) {
        this.nextMap = nextMap;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
