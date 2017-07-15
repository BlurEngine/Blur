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

import com.google.common.collect.Table;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Metadata storage container interface for {@link MetadataHolder}s. See {@link BasicMetadataStorage} for a basic implementation of all
 * functionality.
 * @param <HOLDER> Type of MetadataHolder to map metadata to
 */
public interface MetadataStorage<HOLDER extends MetadataHolder> {

    boolean contains(@Nonnull HOLDER holder);

    boolean contains(@Nonnull HOLDER holder, @Nonnull Object type);

    boolean contains(@Nonnull HOLDER holder, @Nonnull Class<Object> typeClass);

    @Nonnull
    Table<HOLDER, Class<Object>, Object> getAll();

    @Nonnull
    Map<Class<Object>, Object> getMap(@Nonnull HOLDER holder);

    @Nonnull
    List<Object> getList(@Nonnull HOLDER holder);

    @Nonnull
    <T> T get(@Nonnull HOLDER holder, @Nonnull Class<T> typeClass);

    @Nullable
    <T> T put(@Nonnull HOLDER holder, @Nonnull T type);

    @Nonnull
    List<Object> removeAll(@Nonnull HOLDER holder);

    @Nullable
    <T> boolean remove(@Nonnull HOLDER holder, @Nonnull T type);

    @Nullable
    <T> T remove(@Nonnull HOLDER holder, @Nonnull Class<T> typeClass);
}
