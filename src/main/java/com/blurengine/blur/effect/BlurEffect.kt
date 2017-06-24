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

package com.blurengine.blur.effect

import com.blurengine.blur.framework.Component
import com.blurengine.blur.utils.spawnParticleKt
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle

/**
 * Represents a class for creating Particle and other form of effects using [Component]s.
 */
abstract class BlurEffect(val component: Component) {
    /**
     * Starts this effect tickable. Calling this multiple times in the same state causes no change.
     */
    fun start() {
        component.addTickable(this)
    }

    /**
     * Stops this effect tickable. Calling this multiple times in the same state causes no change.
     */
    fun stop() {
        component.removeTickable(this)
    }

    /**
     * Spawns a particle at a location with the given color, doing so successfully requires certain fields to be tailored for it.
     */
    fun spawnColor(particle: Particle, location: Location, color: Color) {
        val offX = color.red / 255f
        val offY = color.green / 255f
        val offZ = color.blue / 255f
        location.world.spawnParticleKt(particle, location, offX = offX, offY = offY, offZ = offZ, count = 0, extra = 1)
    }
}
