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

package com.blurengine.blur.framework;

import com.google.common.base.Preconditions;

import com.supaham.commons.utils.MapBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

public class SerializedModule {

    // TODO be aware of current directory of this context. (Maybe rename this class to include Context)
    private final ModuleLoader moduleLoader;
    private final Object object;

    public SerializedModule(@Nonnull ModuleLoader moduleLoader, @Nullable Object object) {
        this.moduleLoader = Preconditions.checkNotNull(moduleLoader, "moduleLoader cannot be null.");
        this.object = object;
    }

    public Object getAsObject() {
        return object;
    }

    public Integer getAsInteger() {
        return ((Integer) object);
    }

    public Double getAsDouble() {
        return ((Double) object);
    }

    public Float getAsFloat() {
        return ((Float) object);
    }

    public String getAsString() {
        return ((String) object);
    }

    public <E> List<E> getAsList() {
        return ((List<E>) object);
    }

    public Map<String, Object> getAsMap() {
        return ((Map) object);
    }

    public <T> void load(T dataClass) {
        // If data is not map or is empty then don't do anything.
        if (object instanceof Map && getAsMap().size() > 0) {
            moduleLoader.deserializeTo(getAsMap(), dataClass);
        }
    }

    public <T> void loadToField(T dataClass, String fieldName) {
        LinkedHashMap<Object, Object> map = MapBuilder.newLinkedHashMap().put(fieldName, getAsObject()).build();
        moduleLoader.deserializeTo(map, dataClass);
    }

    public <T> void loadWithMap(T dataClass, Map<?, ?> map) {
        moduleLoader.deserializeTo(map, dataClass);
    }

    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public SerializerSet getSerializerSet() {
        return moduleLoader.getSerializerSet();
    }
}
