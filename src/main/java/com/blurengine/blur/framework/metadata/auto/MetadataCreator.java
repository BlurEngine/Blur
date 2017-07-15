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

import com.blurengine.blur.framework.metadata.MetadataHolder;

import javax.annotation.Nonnull;

/**
 * Metadata class instance creator. The supplied class may be of any type, but not null.
 */
public interface MetadataCreator<T, HOLDER extends MetadataHolder> {

    @Nonnull
    T create(HOLDER holder);
}
