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

package com.blurengine.blur.modules.framework;

import com.google.common.base.Preconditions;

import com.supaham.commons.bukkit.utils.SerializationUtils;
import com.supaham.commons.bukkit.utils.VectorUtils;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import pluginbase.config.serializers.Serializer;
import pluginbase.config.serializers.SerializerSet;

/**
 * A simple extension of {@link Serializer} that provides a few utility methods for convenience.
 */
public interface BlurSerializer<T> extends Serializer<T> {

    @Nullable
    @Override
    default Object serialize(@Nullable T object, @NotNull SerializerSet serializerSet) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Nullable
    @Override
    default T deserialize(Object serialized, Class wantedType) {
        return deserialize(serialized, wantedType, SerializationUtils.SERIALIZER_SET);
    }

    /* ================================
     * >> MAP METHODS
     * ================================ */

    default String nullMessage(String key) {
        return key + " is not specified.";
    }

    default int getInt(Map<?, ?> map, String key) {
        return getInt(map, key, nullMessage(key));
    }

    default int getInt(Map<?, ?> map, String key, String nullMessage) {
        Object o = map.get(key);
        Preconditions.checkNotNull(o, nullMessage);
        return Integer.parseInt(o.toString());
    }

    default double getDouble(Map<?, ?> map, String key) {
        return getDouble(map, key, nullMessage(key));
    }

    default double getDouble(Map<?, ?> map, String key, String nullMessage) {
        Object o = map.get(key);
        Preconditions.checkNotNull(o, nullMessage);
        return Double.parseDouble(o.toString());
    }

    default String getString(Map<?, ?> map, String key) {
        return getString(map, key, nullMessage(key));
    }

    default String getString(Map<?, ?> map, String key, String nullMessage) {
        Object o = map.get(key);
        Preconditions.checkNotNull(o, nullMessage);
        return o.toString();
    }

    default Vector getVector(Map<?, ?> map, String key) {
        return getVector(map, key, nullMessage(key));
    }

    default Vector getVector(Map<?, ?> map, String key, String nullMessage) {
        Object o = map.get(key);
        Preconditions.checkNotNull(o, nullMessage);
        return VectorUtils.deserialize(o.toString());
    }
    
    /* ================================
     * >> Delegate convenient methods
     * ================================ */

    default Vector getVector(String string) {
        return VectorUtils.deserialize(string);
    }
}
