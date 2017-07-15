/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.framework.metadata.auto;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.metadata.MetadataHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public abstract class AbstractAutoMetadataCreator<HOLDER extends MetadataHolder> {
    
    private Set<Class<Object>> classes;
    private Map<Class<Object>, MetadataCreator<Object, HOLDER>> creators;

    public AbstractAutoMetadataCreator() {
        this(new HashSet<>(), new HashMap<>());
    }

    protected AbstractAutoMetadataCreator(Set<Class<Object>> classes,
                                          Map<Class<Object>, MetadataCreator<Object, HOLDER>> creators) {
        this.classes = classes;
        this.creators = creators;
    }

    public List<Object> initialize(@Nonnull HOLDER holder) {
        ArrayList<Object> instances = new ArrayList<>();
        for (Class<?> metadataClazz : getClasses()) {
            instances.add(instantiateClass(metadataClazz, holder));
        }
        // Supplier data instances
        for (MetadataCreator<?, HOLDER> creator : creators.values()) {
            instances.add(creator.create(holder));
        }
        return instances;
    }

    @Nonnull
    protected abstract Object instantiateClass(@Nonnull Class<?> clazz, @Nonnull HOLDER holder);

    @Nonnull
    public Set<Class<Object>> getClasses() {
        return Collections.unmodifiableSet(classes);
    }

    @Nonnull
    public Map<Class<Object>, MetadataCreator<Object, HOLDER>> getCreators() {
        return Collections.unmodifiableMap(creators);
    }
    
    public <T> void registerClass(@Nonnull Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        Preconditions.checkArgument(!this.creators.containsKey(clazz),
            "%s already registered (with creator)", clazz);
        Preconditions.checkArgument(!this.classes.contains(clazz),
            "%s already registered (without creator)", clazz);
        this.classes.add((Class<Object>) clazz);
    }

    public <T> void registerClass(@Nonnull Class<T> clazz, @Nonnull MetadataCreator<T, HOLDER> creator) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        Preconditions.checkNotNull(creator, "creator cannot be null.");

        Preconditions.checkArgument(!this.creators.containsKey(clazz),
            "%s already registered (with creator)", clazz);
        Preconditions.checkArgument(!this.classes.contains(clazz),
            "%s already registered (without creator)", clazz);
        this.creators.put((Class) clazz, (MetadataCreator) creator);
    }

    public <T> boolean unregisterClass(@Nonnull Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        if (this.classes.remove(clazz)) {
            return true;
        } else if (this.creators.remove(clazz) != null) {
            return true;
        }
        return false;
    }
}
