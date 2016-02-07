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

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.modules.extents.Extent;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.Serializer;
import pluginbase.config.serializers.SerializerSet;

/**
 * Represents an abstract {@link Serializer} for (de)serializing {@link Extent}s. After creating your serializer, be sure to register it through 
 * {@link ExtentSerializer#registerSerializer(String, Class)} in order for it to be used by Blur.
 */
public abstract class ExtentTypeSerializer<T extends Extent> implements BlurSerializer<T> {

    private final ExtentSerializer parent;

    public ExtentTypeSerializer(ExtentSerializer parent) {
        this.parent = parent;
    }

    @Override // serialize(Object) already provided by PB Serializer interface
    public Object serialize(T object) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    protected T deserialize(Object object) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Nullable
    @Override
    public Object serialize(@Nullable T object, @Nonnull SerializerSet serializerSet) {
        return object == null ? null : serialize(object);
    }

    @Nullable
    @Override
    public T deserialize(@Nullable Object serialized, @Nonnull Class wantedType, @Nonnull SerializerSet serializerSet) {
        if (serialized == null) {
            return null;
        }
        deserializedPreHandler(serialized);
        return deserialize(serialized);
    }

    protected void deserializedPreHandler(Object o) {
        Preconditions.checkArgument(o instanceof Map, "serialized object must be a map.");
    }

    public ExtentSerializer getParentSerializer() {
        return parent;
    }
}
