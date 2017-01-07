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

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.extents.BlockExtent;
import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.Filters;
import com.blurengine.blur.modules.spawns.SpawnDirection.NullSpawnDirection;

import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * Represents a spawnpoint used by {@link SpawnsModule}.
 */
public class Spawn {

    public static final Spawn ZERO = new Spawn(BlockExtent.ZERO);

    private final Extent extent;
    private final SpawnDirection spawnDirection;
    private final Filter filter;

    public Spawn(@Nonnull Extent extent) {
        this(extent, NullSpawnDirection.INSTANCE, Filters.ALWAYS_ALLOW);
    }

    public Spawn(@Nonnull Extent extent, @Nonnull SpawnDirection spawnDirection, @Nonnull Filter filter) {
        this.extent = Preconditions.checkNotNull(extent, "extent cannot be null.");
        this.spawnDirection = Preconditions.checkNotNull(spawnDirection, "spawnDirection cannot be null.");
        this.filter = Preconditions.checkNotNull(filter, "filter cannot be null.");
    }
    
    public Extent getExtent() {
        return extent;
    }

    public SpawnDirection getSpawnDirection() {
        return spawnDirection;
    }

    public Filter getFilter() {
        return filter;
    }
}
