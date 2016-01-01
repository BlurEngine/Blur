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

package com.blurengine.blur.modules.filters.serializer;

import com.blurengine.blur.modules.extents.UnionExtent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import pluginbase.config.serializers.SerializerSet;

final class FilterSerializers {

    static final class Team extends FilterTypeSerializer<UnionExtent> {

        public Team(FilterSerializer parent) {
            super(parent);
        }

        @Override
        public UnionExtent deserialize(@Nullable Object serialized, @NotNull Class wantedType, @NotNull SerializerSet serializerSet)
            throws IllegalArgumentException {
            if (serialized instanceof List) {

            }
            return null;
        }
    }
}
