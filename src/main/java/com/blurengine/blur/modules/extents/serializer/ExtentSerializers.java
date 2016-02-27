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

import com.blurengine.blur.modules.extents.AutoCircleExtent;
import com.blurengine.blur.modules.extents.BlockExtent;
import com.blurengine.blur.modules.extents.CuboidExtent;
import com.blurengine.blur.modules.extents.CylinderExtent;
import com.blurengine.blur.modules.extents.UnionExtent;
import com.supaham.commons.bukkit.utils.ImmutableBlockVector;
import com.supaham.commons.bukkit.utils.ImmutableVector;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

final class ExtentSerializers {

    static final class Union extends ExtentTypeSerializer<UnionExtent> {

        public Union(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        public UnionExtent deserialize(@Nullable Object serialized, @Nonnull Class wantedType, @Nonnull SerializerSet serializerSet)
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
            Object height = map.get("height");
            height = height == null ? 1D : Double.parseDouble(height.toString());
            return new CylinderExtent(new ImmutableVector(getVector(map, "base")), getDouble(map, "radius"), (Double) height);
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

    static final class AutoCircle extends ExtentTypeSerializer<AutoCircleExtent> {

        public AutoCircle(ExtentSerializer parent) {
            super(parent);
        }

        @Override
        protected AutoCircleExtent deserialize(Object object) {
            Map map = (Map) object;
            Vector base = getVector(map, "base");
            double radius = getDouble(map, "radius");
            Preconditions.checkArgument(radius > 0, "radius must be greater than 0.");

            int points = getInt(map, "points");
            Preconditions.checkArgument(points > 0, "points must be greater than 0.");

            double offsetAngle = Optional.ofNullable(map.get("offset-angle")).map(Object::toString).map(Double::parseDouble).orElse(0.);
            double x = base.getX();
            double z = base.getZ();

            double offsetRadians = Math.toRadians(offsetAngle); // Must convert offset in degrees to radians to comply with minecraft.
            List<Vector> pointsList = IntStream.range(0, points).mapToObj(i -> {
                double angle = ((double) i / points) * Math.PI * 2 + offsetRadians;
                double dX = Math.cos(angle) * radius + x;
                double dZ = Math.sin(angle) * radius + z;
                return new Vector(dX, base.getY(), dZ);
            }).collect(Collectors.toList());
            return new AutoCircleExtent(base, pointsList, radius, offsetRadians);
        }
    }
}
