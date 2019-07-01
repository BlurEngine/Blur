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
import com.supaham.commons.Enums
import com.supaham.commons.bukkit.utils.ImmutableBlockVector
import com.supaham.commons.bukkit.utils.ImmutableVector
import org.bukkit.Effect
import org.bukkit.Particle
import org.bukkit.util.BlockVector
import java.time.Duration
import java.util.ArrayList

@ModuleInfo(name = "SimpleParticles", dataClass = SimpleParticlesData::class)
class SimpleParticlesModule(manager: ModuleManager, val data: SimpleParticlesData) : WorldModule(manager) {
    private val outlines: ListMultimap<Extent, BlockVector> = ArrayListMultimap.create()

    init {
        first@data.particles.forEach { pdata ->
            outlines.putAll(pdata.extent, doExtent(pdata.extent))
            newTask({
                outlines.get(pdata.extent).forEach {
                    world.spawnParticle(pdata.particle!!, it.toLocation(this.world), 2, 0.0, 0.0, 0.0, 0.0)
                }
            }).interval(pdata.interval).build()
        }
    }
    
    fun doExtent(extent: Extent?): List<BlockVector> {
        return extent!!.toList()
    }

    class SimpleParticlesData : ModuleData {
        var particles = arrayListOf<ExtentParticles>()
        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module? {
            if (serialized.asObject !is Map<*, *> || serialized.asMap.containsKey("particles").not()) {
                check(serialized.asObject is List<*>, "SimpleParticles data must either be list or map with key \"particles\".")
                serialized.setObject(mapOf("particles" to serialized.getAsList<Any>()))
            }
            (serialized.asMap["particles"] as List<Map<*, *>>)
                    .filter { it["particle"] != null }
                    .map { it["particle"].toString() }
                    .filter { Enums.findFuzzyByValue(Particle::class.java, it) == null }
                    .forEach { moduleManager.logger.severe("Unknown particle type $it") } // TODO Remove item?
            serialized.load(this)
            return SimpleParticlesModule(moduleManager, this)
        }
    }

    class ExtentParticles {
        var particle: Particle? = null
        var interval: Duration = Duration.ofSeconds(1)
        var extent: Extent? = null
    }
}
