/*
 * Copyright 2018 Ali Moghnieh
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

package com.blurengine.blur.modules.extents

import com.blurengine.blur.modules.spawns.Spawn
import com.supaham.commons.bukkit.utils.ImmutableVector
import com.supaham.commons.bukkit.utils.RelativeVector

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

/**
 * Represents a [Spawn] direction.
 */
interface ExtentDirection {

    fun applyTo(location: Location, entity: Entity?)

    object NullExtentDirection : ExtentDirection {
        override fun applyTo(location: Location, entity: Entity?) {}
    }

    /**
     * Represents a fixed [ExtentDirection] implementation.
     */
    class FixedExtentDirection(val yaw: Float, val pitch: Float) : ExtentDirection {

        override fun applyTo(location: Location, entity: Entity?) {
            location.yaw = yaw
            location.pitch = pitch
        }
    }

    /**
     * Represents a dynamic [ExtentDirection] implementation where the [Location] yaw and pitch get realigned to point towards the given
     * vector.
     */
    class PointToExtentDirection(val vector: ImmutableVector) : ExtentDirection {

        override fun applyTo(location: Location, entity: Entity?) {
            var target = vector
            if (vector is RelativeVector) {
                target = if (entity is LivingEntity) {
                    vector.with(entity.eyeLocation.toVector())
                } else {
                    vector.with(location.toVector())
                }
            }

            val target2 = location.toVector().subtract(target.toVector())
            var angle = Math.atan2(target2.z, target2.x).toFloat()
            angle += (Math.PI / 2.0).toFloat() // add quarter of circle
            angle = Math.toDegrees(angle.toDouble()).toFloat()
            if (angle < 0) {
                angle += 360f
            }

            val relativePos = Vector(0.0, 0.0, 0.0)
            if (entity != null) {
                relativePos.y = (entity as LivingEntity).eyeHeight
            }
            val baseVector = location.toVector().add(relativePos)
            val offset = target.subtract(baseVector).toLocation(location.world)
            val pitch = if (offset.lengthSquared() > 0) {
                (-Math.toDegrees(Math.asin(offset.y / offset.length()))).toFloat()
            } else {
                entity?.location?.pitch ?: 0f
            }
            location.yaw = angle
            location.pitch = pitch
        }
    }
}
