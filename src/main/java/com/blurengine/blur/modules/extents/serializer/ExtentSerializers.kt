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

package com.blurengine.blur.modules.extents.serializer

import com.blurengine.blur.modules.extents.AutoCircleExtent
import com.blurengine.blur.modules.extents.BlockExtent
import com.blurengine.blur.modules.extents.CuboidExtent
import com.blurengine.blur.modules.extents.CylinderExtent
import com.blurengine.blur.modules.extents.ExtentDirection
import com.blurengine.blur.modules.extents.ExtentDirection.NullExtentDirection
import com.blurengine.blur.modules.extents.UnionExtent
import com.google.common.base.Preconditions
import com.supaham.commons.bukkit.utils.ImmutableVector
import org.bukkit.util.BlockVector
import pluginbase.config.serializers.SerializerSet

internal class ExtentSerializers {

    internal class Union(parent: ExtentSerializer) : ExtentTypeSerializer<UnionExtent>(parent) {

        @Throws(IllegalArgumentException::class)
        override fun deserialize(serialized: Any?, wantedType: Class<*>, serializerSet: SerializerSet): UnionExtent {
            return if (serialized is List<*>) {
                val extents = serialized.map { parentSerializer.deserialize(it, wantedType, serializerSet) }
                UnionExtent(extents)
            } else {
                val singleExtent = parentSerializer.deserialize(serialized, wantedType, serializerSet)
                UnionExtent(singleExtent)
            }
        }
    }

    internal class Cuboid(parent: ExtentSerializer) : ExtentTypeSerializer<CuboidExtent>(parent) {

        @Throws(IllegalArgumentException::class)
        public override fun deserialize(serialized: Any): CuboidExtent {
            val map = serialized as Map<*, *>
            val direction = deserializeExtentDirection(serialized)
            return CuboidExtent(getVector(map, "min"), getVector(map, "max"), direction)
        }
    }

    internal class Cylinder(parent: ExtentSerializer) : ExtentTypeSerializer<CylinderExtent>(parent) {

        @Throws(IllegalArgumentException::class)
        public override fun deserialize(serialized: Any): CylinderExtent {
            val map = serialized as Map<*, *>
            val direction = deserializeExtentDirection(serialized)
            var height: Any? = map["height"]
            height = if (height == null) 1.0 else java.lang.Double.parseDouble(height.toString())
            return CylinderExtent(ImmutableVector(getVector(map, "base")), getDouble(map, "radius"), (height as Double?)!!, direction)
        }
    }

    internal class Block(parent: ExtentSerializer) : ExtentTypeSerializer<BlockExtent>(parent) {

        @Throws(IllegalArgumentException::class)
        public override fun deserialize(serialized: Any): BlockExtent {
            val bv: BlockVector
            val direction: ExtentDirection
            when (serialized) {
                is Map<*, *> -> {
                    bv = BlockVector(getVector(serialized, "base"))
                    direction = deserializeExtentDirection(serialized)
                }
                is String -> {
                    val position = getPosition(serialized.toString())
                    bv = BlockVector(position.xAsInt, position.yAsInt, position.zAsInt)
                    direction = if (position.yaw != 0f || position.pitch != 0f) {
                        ExtentDirection.FixedExtentDirection(position.yaw, position.pitch)
                    } else {
                        NullExtentDirection
                    }
                }
                else -> throw IllegalArgumentException(
                        "Unexpected data type of " + serialized.javaClass.name + " when deserializing Block extent.")
            }
            return BlockExtent(ImmutableVector(bv), direction)
        }

        override fun deserializedPreHandler(o: Any) {} // No data type requirements
    }

    internal class AutoCircle(parent: ExtentSerializer) : ExtentTypeSerializer<AutoCircleExtent>(parent) {

        override fun deserialize(`object`: Any): AutoCircleExtent {
            val map = `object` as Map<*, *>
            val base = getVector(map, "base")
            val radius = getDouble(map, "radius")
            Preconditions.checkArgument(radius > 0, "radius must be greater than 0.")

            val points = getInt(map, "points")
            Preconditions.checkArgument(points > 0, "points must be greater than 0.")

            val offsetAngle = map["offset-angle"]?.run { toString().toDouble() } ?: 0.0

            val offsetRadians = Math.toRadians(offsetAngle) // Must convert offset in degrees to radians to comply with minecraft.
            val direction = deserializeExtentDirection(map)
            return AutoCircleExtent(base, points, radius, offsetRadians, direction)
        }
    }
}
