/*
 * Copyright 2019 Ali Moghnieh
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

import com.blurengine.blur.utils.add
import com.supaham.commons.bukkit.utils.ImmutableVector
import org.bukkit.entity.Entity
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import java.util.Random

/**
 * Interface for representing remote [Extent]s through the world.
 */
interface RemoteExtent : Extent

/**
 * Represents a [RemoteExtent] abstract implementation that uses an initial and offset vector. See [EntityAttachedExtent] for an example
 * implementation.
 */
abstract class AbstractRemoteExtent(val extent: Extent) : RemoteExtent {
    abstract val initial: ImmutableVector
    abstract val offset: ImmutableVector

    override fun contains(x: Double, y: Double, z: Double): Boolean {
        val diffX = offset.x - initial.x
        val diffY = offset.y - initial.y
        val diffZ = offset.z - initial.z
        return extent.contains(x - diffX, y - diffY, z - diffZ)
    }

    // TODO maybe?
    override fun getVolume(): Double = extent.volume

    override fun getRandomLocation(random: Random?): Vector {
        val diffX = offset.x - initial.x
        val diffY = offset.y - initial.y
        val diffZ = offset.z - initial.z
        val result = extent.getRandomLocation(random)
        result.add(diffX, diffY, diffZ)
        return result
    }

    override fun iterator(): MutableIterator<BlockVector> {
        val it = extent.iterator()

        return object : MutableIterator<BlockVector> {
            override fun hasNext(): Boolean {
                return it.hasNext()
            }

            override fun next(): BlockVector {
                val diffX = offset.blockX - initial.blockX
                val diffY = offset.blockY - initial.blockY
                val diffZ = offset.blockZ - initial.blockZ
                val next = it.next()
                next.add(x = diffX, y = diffY, z = diffZ)
                return next
            }

            override fun remove() {
                it.remove()
            }
        }
    }
}

/**
 * Implementation of [RemoteExtent] using attachment to [Entity]. Every time [offset] is called, the current location of the entity is returned.
 * 
 * Note: You must maintain this reference if the entity is no longer existent.
 */
class EntityAttachedExtent(val attachedTo: Entity, extent: Extent) : AbstractRemoteExtent(extent) {

    override val initial = ImmutableVector(attachedTo.location.toVector())
    // Single instance for all location calls
    private val _location = attachedTo.location

    override val offset: ImmutableVector
        get() {
            attachedTo.getLocation(_location)
            return ImmutableVector(_location.x, _location.y, _location.z)
        }
}
