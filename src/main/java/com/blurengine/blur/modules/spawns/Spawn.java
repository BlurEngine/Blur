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

package com.blurengine.blur.modules.spawns;

import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.modules.extents.BlockExtent;

import lombok.Getter;

/**
 * Represents a spawnpoint used by {@link SpawnsModule}.
 */
@Getter
public class Spawn {

    public static final Spawn ZERO = new Spawn(BlockExtent.ZERO);

    private final Extent extent;
    // TODO add support for point-to, allowing dynamic directions
    private float yaw;
    private float pitch;

    public Spawn(Extent extent) {
        this.extent = extent;
    }

    public Spawn(Extent extent, float yaw, float pitch) {
        this.extent = extent;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
