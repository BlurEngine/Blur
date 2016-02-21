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

package com.blurengine.blur.modules.extents.serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.modules.extents.ExtentManager;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.AutoCircle;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Block;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Cuboid;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Cylinder;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Union;
import com.blurengine.blur.serializers.ExtentList;
import com.supaham.commons.bukkit.utils.SerializationUtils;
import com.supaham.commons.utils.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

/**
 * Represents an {@link Extent} serializer. Keep in mind this is for single extents. For a {@link List} of extents, see {@link ExtentList}.
 */
public class ExtentSerializer implements BlurSerializer<Extent> {

    private static final String[] RESERVED_SERIALIZERS = new String[]{"cuboid", "union", "cylinder", "block"};
    private static final Map<String, Class<? extends ExtentTypeSerializer>> EXTENT_SERIALIZERS = new HashMap<>();

    private final ModuleLoader moduleLoader;
    private final Map<String, ExtentTypeSerializer<?>> serializers;

    static {

        EXTENT_SERIALIZERS.put("cuboid", Cuboid.class);
        EXTENT_SERIALIZERS.put("union", Union.class);
        EXTENT_SERIALIZERS.put("cylinder", Cylinder.class);
        EXTENT_SERIALIZERS.put("block", Block.class);
        EXTENT_SERIALIZERS.put("auto-circle", AutoCircle.class);
    }

    /**
     * Registers a clazz for a {@link ExtentSerializer} to use when deserializing. The {@code extentType} is the name of the extent type, e.g.
     * union, that will be handled by the given clazz.
     *
     * @param extentType name of region type that is handled by the given {@code clazz}
     * @param clazz clazz to handle the {@code extentType}
     *
     * @return previous clazz that was removed by this execution
     */
    public static Class<? extends ExtentTypeSerializer> registerSerializer(String extentType, Class<? extends ExtentTypeSerializer> clazz) {
        Preconditions.checkNotNull(extentType, "extentType cannot be null.");
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        extentType = extentType.toLowerCase();
        Preconditions.checkArgument(ArrayUtils.contains(RESERVED_SERIALIZERS, extentType), "%s is a reserved extent type.", extentType);
        return EXTENT_SERIALIZERS.put(extentType, clazz);
    }

    public ExtentSerializer(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
        Map<String, ExtentTypeSerializer<?>> serializers = new HashMap<>();
        EXTENT_SERIALIZERS.forEach((k, v) -> {
            try {
                serializers.put(k, v.getDeclaredConstructor(ExtentSerializer.class).newInstance(this));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        });
        this.serializers = ImmutableMap.copyOf(serializers);
    }

    @Override
    public Extent deserialize(@Nullable Object serialized, @Nonnull Class wantedType, @Nonnull SerializerSet serializerSet) {
        if (serialized == null) {
            return null;
        } else if (serialized instanceof Map) {
            return deserializeExtent((Map<String, Object>) serialized);
        } else if (serialized instanceof String) {
            return deserializeExtent(ImmutableMap.of("id", serialized.toString()));
        }
        throw new IllegalArgumentException("Expected List or Map, got data type of " + serialized.getClass().getName() + ".");
    }

    /**
     * Deals in {@link ExtentReference} to allow extents to be referenced before they're defined for user convenience. The deserialized extent
     * is then added to this serializer for later use.
     * <p />
     * The given {@code map} must either include the key <em>id</em>, otherwise a registered serializer for any of the given key.
     *
     * @param map map of serialized extent
     *
     * @return {@link ExtentReference} which may have either id or extent set, or both if it's a definition with id.
     */
    public Extent deserializeExtent(Map<String, Object> map) {
        List<String> invalidTypes = new ArrayList<>();

        Optional<String> id = map.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("id")).findFirst().map(e -> e.getValue().toString());

        Extent extent = null;
        for (Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equalsIgnoreCase("id")) {
                continue;
            }
            // Get serializer by the name of the given key.
            ExtentTypeSerializer<?> serializer = serializers.get(e.getKey());
            if (serializer == null) {
                invalidTypes.add(e.getKey());
            } else {
                extent = serializer.deserialize(e.getValue(), null, SerializationUtils.SERIALIZER_SET);
            }
        }

        // Notify the user of defined invalid extent types.
        if (!invalidTypes.isEmpty()) {
            moduleLoader.getLogger().warning("Unknown extent types: " + invalidTypes);
        }

        // If no extent was defined, then we must have an id reference
        if (extent == null) {
            String extentId = id.orElseThrow(() -> new NullPointerException("no extent id or extent definition."));
            extent = getManager().getNonNullExtentById(extentId);
        }
        // Extent was defined, add it.
        else {
            String idStr = id.orElse(null);
            getManager().addExtent(idStr, extent);
        }
        return extent;
    }

    public ExtentManager getManager() {
        return this.moduleLoader.getModuleManager().getExtentManager();
    }
}
