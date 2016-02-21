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

package com.blurengine.blur.modules.misc

import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.modules.extents.CuboidExtent
import com.blurengine.blur.modules.extents.CylinderExtent
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.extents.UnionExtent
import com.blurengine.blur.modules.misc.SimpleParticlesModule.SimpleParticlesData
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.supaham.commons.bukkit.utils.ImmutableBlockVector
import com.supaham.commons.bukkit.utils.ImmutableVector
import org.bukkit.Effect
import java.time.Duration
import java.util.ArrayList

@ModuleInfo(name = "SimpleParticles", dataClass = SimpleParticlesData::class)
class SimpleParticlesModule(manager: ModuleManager, val data: SimpleParticlesData) : WorldModule(manager) {
    private val outlines: ListMultimap<Extent, ImmutableVector> = ArrayListMultimap.create()

    init {
        first@data.particles.forEach { pdata ->
            outlines.putAll(pdata.extent, doExtent(pdata.extent))
            newTask({
                outlines.get(pdata.extent).forEach {
                    world.spigot().playEffect(it.toLocation(this.world), pdata.particle, 0, 0, 0f, 0f, 0f, 0f, 2, 64)
                }
            }).interval(pdata.interval).build()
        }
    }

    fun doExtent(extent: Extent?): List<ImmutableVector> {
        return when (extent) {
            is UnionExtent -> doUnion(extent)
            is CylinderExtent -> doCylinder(extent)
            is CuboidExtent -> doCuboid(extent)
            else -> emptyList()
        }
    }

    fun doUnion(extent: UnionExtent): List<ImmutableVector> {
        val result = ArrayList<ImmutableVector>()
        extent.extents.forEach { result.addAll(doExtent(it)) }
        return result
    }

    fun doCylinder(extent: CylinderExtent): List<ImmutableVector> {
        val result = ArrayList<ImmutableVector>()
        val spacing = 20
        (0..spacing).map {
            val angle = (it.toDouble() / spacing) * Math.PI * 2.0
            val dX = Math.cos(angle) * extent.radius + extent.base.x
            val dZ = Math.sin(angle) * extent.radius + extent.base.z
            val y = extent.base.y
            (0..extent.height.toInt()).forEach {
                result.add(ImmutableVector(dX, extent.base.y + it, dZ))
            }
        }
        return result
    }

    fun doCuboid(extent: CuboidExtent): List<ImmutableVector> {
        val result = ArrayList<ImmutableVector>()
        val min = extent.minimumPoint
        val max = extent.maximumPoint
        (min.y.toInt()..max.y.toInt()).forEach { y ->
            // Top Left to top right
            (min.blockX..max.blockX).forEach { x ->
                result.add(ImmutableBlockVector(x, y, min.z.toInt()))
            }
            // Top right to bottom right
            (max.blockZ downTo min.blockZ).forEach { z ->
                result.add(ImmutableBlockVector(max.blockX, y, z))
            }
            // Bottom right to bottom left
            (min.blockZ..max.blockZ).forEach { z ->
                result.add(ImmutableBlockVector(min.blockX, y, z))
            }
            // Bottom left to top left
            (max.blockX downTo min.blockX).forEach { x ->
                result.add(ImmutableBlockVector(x, y, max.z.toInt()))
            }
        }
        // LOOP THROUGH WALLS AND MULTIPLY BY HEIGHT
        return result
    }

    class SimpleParticlesData : ModuleData {
        var particles = arrayListOf<ExtentParticles>()
        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module? {
            serialized.load(this)
            return SimpleParticlesModule(moduleManager, this)
        }
    }

    class ExtentParticles {
        var particle: Effect? = null
        var interval: Duration = Duration.ofSeconds(1)
        var extent: Extent? = null
    }
}
