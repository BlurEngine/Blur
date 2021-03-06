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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an entity that may hold metadata. The implementation should aim to use {@link MetadataStorage} for management of this data.
 */
public interface MetadataHolder {

    boolean hasMetadata(@Nonnull Object object);

    <T> boolean hasMetadata(@Nonnull Class<T> metadataClass);

    <T> T getMetadata(@Nonnull Class<T> metadataClass);

    @Nullable
    Object putMetadata(Object object);

    @Nonnull
    List<Object> removeAll();

    <T> boolean removeMetadata(T object);

    @Nullable
    <T> T removeMetadata(Class<T> metadataClass);
}
