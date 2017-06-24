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

package com.blurengine.blur.framework.metadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BasicMetadataStorage<HOLDER extends MetadataHolder> implements MetadataStorage<HOLDER> {

    private Table<HOLDER, Class<Object>, Object> metadata;

    public BasicMetadataStorage() {
        this(HashBasedTable.create());
    }

    public BasicMetadataStorage(@Nonnull Table<HOLDER, Class<Object>, Object> metadata) {
        this.metadata = Preconditions.checkNotNull(metadata, "metadata cannot be null.");
    }

    @Override
    public boolean contains(@Nonnull HOLDER holder) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        return metadata.containsRow(holder);
    }

    @Override
    public boolean contains(@Nonnull HOLDER holder, @Nonnull Object type) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Preconditions.checkNotNull(type, "type cannot be null.");
        return metadata.contains(holder, type.getClass());
    }

    @Override
    public boolean contains(@Nonnull HOLDER holder, @Nonnull Class<Object> typeClass) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Preconditions.checkNotNull(typeClass, "typeClass cannot be null.");
        return metadata.contains(holder, typeClass);
    }

    @Nonnull
    @Override
    public Table<HOLDER, Class<Object>, Object> getAll() {
        return Tables.unmodifiableTable(metadata);
    }

    @Nonnull
    @Override
    public Map<Class<Object>, Object> getMap(@Nonnull HOLDER holder) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        return Collections.unmodifiableMap(metadata.row(holder));
    }

    @Nonnull
    @Override
    public List<Object> getList(@Nonnull HOLDER holder) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Map<Class<Object>, Object> mapFor = metadata.row(holder);
        return Collections.unmodifiableList(new ArrayList<>(mapFor.values()));
    }

    @Nonnull
    @Override
    public <T> T get(@Nonnull HOLDER holder, Class<T> typeClass) {
        return (T) metadata.get(holder, typeClass);
    }

    @Nullable
    @Override
    public <T> T put(@Nonnull HOLDER holder, @Nonnull T type) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Preconditions.checkNotNull(type, "type cannot be null.");
        return (T) metadata.put(holder, (Class<Object>) type.getClass(), type);
    }

    @Nullable
    @Override
    public List<Object> removeAll(@Nonnull HOLDER holder) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Map<Class<Object>, Object> row = metadata.row(holder);
        List<Object> types = new ArrayList<>(row.values());
        row.clear();
        return Collections.unmodifiableList(types);
    }

    @Nullable
    @Override
    public <T> boolean remove(@Nonnull HOLDER holder, @Nonnull T type) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Preconditions.checkNotNull(type, "type cannot be null.");
        return metadata.remove(holder, type.getClass()) != null;
    }

    @Nullable
    @Override
    public <T> T remove(@Nonnull HOLDER holder, @Nonnull Class<T> typeClass) {
        Preconditions.checkNotNull(holder, "holder cannot be null.");
        Preconditions.checkNotNull(typeClass, "typeClass cannot be null.");
        return (T) metadata.remove(holder, typeClass);
    }
}
