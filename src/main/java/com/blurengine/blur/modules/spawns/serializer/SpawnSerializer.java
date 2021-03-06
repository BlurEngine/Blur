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

package com.blurengine.blur.modules.spawns.serializer;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.modules.extents.ExtentNotFoundException;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.Filters;
import com.blurengine.blur.modules.spawns.Spawn;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

/**
 * Represents a {@link Spawn} serializer. Keep in mind this is for single spawns. For a {@link List} of spawns, see {@link SpawnList}.
 */
@SuppressWarnings("Duplicates")
public class SpawnSerializer implements BlurSerializer<Spawn> {

    private final ModuleLoader moduleLoader;

    public SpawnSerializer(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
    }

    @Override
    public Spawn deserialize(@Nullable Object serialized, @Nonnull Class wantedType, @Nonnull SerializerSet serializerSet) {
        return getSpawn(serialized);
    }

    /* ================================
     * >> Deserialization methods
     * ================================ */


    public Spawn deserializeMapToSpawn(Map<String, Object> map) {
        // Look for a provided extent, if no extent was found, pass null for the Extent parameter.
        Optional<Object> _extent = Optional.ofNullable(map.remove("extent")); // Remove extent to skip the extent deserialization in FieldMapper.
        return deserializeMapToSpawn(_extent.map(this::getExtent).orElse(null), map);
    }

    public Spawn deserializeMapToSpawn(Extent extent, Map<String, Object> map) {
        Preconditions.checkArgument(!map.isEmpty(), "given map is empty.");

        SpawnData destination = new SpawnData(extent);
        if (map.containsKey("filter")) {
            destination.filter = this.moduleLoader.getFilterSerializer().deserialize(map.get("filter"), Filter.class);
        }
        moduleLoader.deserializeTo(map, destination);
        return destination.toSpawn();
    }

    public Spawn deserializeSingleMapEntryToSpawn(Map<String, Object> map) {
        Entry<String, Object> entry = map.entrySet().iterator().next();
        String key = entry.getKey();
        Object value = entry.getValue();

        // Simple {extent: "id"/definition} map.
        if (key.equalsIgnoreCase("extent")) {
            // This doesn't use getSpawn() because the context we're deserializing is only Extent.
            return new Spawn(getExtent(value));
        }

        String id = key;
        Preconditions.checkArgument(value instanceof Map, "Spawn value of %s must be Map. Got type %s", id, value.getClass().getName());
        Extent extent = getExtentById(id);
        return deserializeMapToSpawn(extent, (Map<String, Object>) value);
    }

    /* ================================
     * >> UTILS
     * ================================ */
    private Extent getExtentById(String id) throws ExtentNotFoundException {
        return moduleLoader.getModuleManager().getExtentManager().getExtentById(id);
    }

    private Extent getExtentByString(String id) throws ExtentNotFoundException {
        return moduleLoader.getModuleManager().getExtentManager().getExtentByString(id);
    }

    @Nonnull
    private Extent getExtent(Object object) {
        return moduleLoader.getModuleManager().getExtentManager().getOrCreateExtent(object);
    }

    private Spawn getSpawn(Object serialized) {
        if (serialized == null) {
            return null;
        } else if (serialized instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) serialized;
            // Complex: "extent-name": {spawn-data}
            // or
            // Simple: {extent: "id"/definition} map.
            if (map.size() == 1) {
                return deserializeSingleMapEntryToSpawn(map);
            }
            // More than just the Extent is provided for the Spawn Object.
            return deserializeMapToSpawn(map);
        } else if (serialized instanceof String) {
            return new Spawn(getExtentByString(serialized.toString()));
        } else {
            throw new IllegalArgumentException("Expected String or Map, got Spawn data type of " + serialized.getClass().getName() + ".");
        }
    }

    private static class SpawnData {

        private Extent extent;
        private transient Filter filter = Filters.ALWAYS_ALLOW;

        private SpawnData() {}

        public SpawnData(Extent extent) {
            this.extent = extent;
        }

        public Spawn toSpawn() {
            return new Spawn(extent, filter);
        }
    }
}
