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
import com.supaham.commons.bukkit.TickerTask
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle

/**
 * Represents a class for creating Particle and other form of effects using [Component]s.
 */
abstract class BlurEffect(val component: Component, delay: Int = 0, interval: Int = 1, var iterations: Int = 0) : Runnable {
    var delay: Int = delay
        set(value) {
            field = value
        }
    var interval: Int = interval
        set(value) {
            field = value
        }

    var remIterations: Int = iterations

    private var task: TickerTask? = null
    val started: Boolean get() = task?.isStarted ?: false
    /**
     * Returns whether this effect is complete as a result of the `iterations` processed.
     */
    var complete: Boolean = false
        private set

    /**
     * Starts this effect tickable. Calling this multiple times in the same state causes no change.
     */
    fun start() {
        require(!started) { "BlurEffect already started." }

        remIterations = iterations
        complete = false
        val task = TickerTask(component.session.blur.plugin, delay.toLong(), interval.toLong(), Runnable { tick() })
        this.task = task
        component.addTask(task)
    }

    /**
     * Stops this effect tickable. Calling this multiple times in the same state causes no change.
     */
    fun stop() {
        require(started) { "BlurEffect is not started." }
        component.removeTask(this.task!!)
        this.task = null
    }

    private fun tick() {
        if (!started || complete) return

        var isComplete = false
        if (remIterations > 0) {
            if (--remIterations <= 0) {
                isComplete = true
            }
        }

        run() // subclass Effect implementation

        if (isComplete) {
            complete = true
            stop()
        }
    }

    /**
     * Spawns a particle at a location with the given color, doing so successfully requires certain fields to be tailored for it.
     */
    fun spawnColor(particle: Particle, location: Location, color: Color) {
        location.world.spawnParticleKt(particle, location, count = 0, extra = 1, data = Particle.DustOptions(color, 1.0f))
    }
}
