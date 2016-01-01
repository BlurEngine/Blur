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

import com.blurengine.blur.modules.extents.CylinderExtent;
import com.blurengine.blur.modules.extents.BlockExtent;
import com.blurengine.blur.modules.extents.CuboidExtent;
import com.blurengine.blur.modules.extents.UnionExtent;
import com.supaham.commons.bukkit.utils.ImmutableBlockVector;
import com.supaham.commons.bukkit.utils.ImmutableVector;

import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pluginbase.config.serializers.SerializerSet;

final class ExtentSerializers {

    static final class Union extends ExtentTypeSerializer<UnionExtent> {

        public Union(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        public UnionExtent deserialize(@Nullable Object serialized, @NotNull Class wantedType, @NotNull SerializerSet serializerSet)
            throws IllegalArgumentException {
            if (serialized instanceof List) {

                return new UnionExtent(((List<Object>) serialized).stream()
                    .map(o -> getParentSerializer().deserializeExtent((Map<String, Object>) o)).collect(Collectors.toList()));
            }
            return null;
        }
    }

    static final class Cuboid extends ExtentTypeSerializer<CuboidExtent> {

        public Cuboid(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        public CuboidExtent deserialize(Object serialized) throws IllegalArgumentException {
            Map<?, ?> map = (Map) serialized;
            return new CuboidExtent(getVector(map, "min"), getVector(map, "max"));
        }
    }

    static final class Cylinder extends ExtentTypeSerializer<CylinderExtent> {

        public Cylinder(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        public CylinderExtent deserialize(Object serialized) throws IllegalArgumentException {
            Map<?, ?> map = (Map) serialized;
            return new CylinderExtent(new ImmutableVector(getVector(map, "base")), getDouble(map, "radius"), getDouble(map, "height"));
        }
    }

    static final class Block extends ExtentTypeSerializer<BlockExtent> {

        public Block(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        public BlockExtent deserialize(Object serialized) throws IllegalArgumentException {
            BlockVector bv;
            if (serialized instanceof Map) {
                Map<?, ?> map = (Map) serialized;
                bv = new BlockVector(getVector(map, "base"));
            } else if (serialized instanceof String) {
                bv = new BlockVector(getVector(serialized.toString()));
            } else {
                throw new IllegalArgumentException(
                    "Unexpected data type of " + serialized.getClass().getName() + " when deserializing Block extent.");
            }
            return new BlockExtent(new ImmutableBlockVector(bv));
        }

        @Override
        protected void deserializedPreHandler(Object o) {} // No data type requirements
    }
}
