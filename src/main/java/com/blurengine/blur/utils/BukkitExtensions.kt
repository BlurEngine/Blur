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

package com.blurengine.blur.utils

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/* ================================
 * >> Space
 * ================================ */

fun Location.add(x: Number = 0, y: Number = 0, z: Number = 0): Location = add(x.toDouble(), y.toDouble(), z.toDouble())

fun Location.subtract(x: Number = 0, y: Number = 0, z: Number = 0): Location = subtract(x.toDouble(), y.toDouble(), z.toDouble())

fun Location.center(centerY: Boolean = false) = this.apply { x = blockX + 0.5; if (centerY) y = blockY + 0.5; z = blockZ + 0.5 }

fun Vector.center(centerY: Boolean = false) = this.apply { x = blockX + 0.5; if (centerY) y = blockY + 0.5; z = blockZ + 0.5 }

fun Vector.add(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0) = this.apply {
    this.x += x.toDouble()
    this.y += y.toDouble()
    this.z += z.toDouble()
}

fun Vector.subtract(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0) = this.apply {
    this.x -= x.toDouble()
    this.y -= y.toDouble()
    this.z -= z.toDouble()
}

operator fun Location.times(multiplicand: Number): Location = let {
    val m = multiplicand.toDouble()
    Location(world, x * m, y * m, z * m, yaw, pitch)
}

operator fun Location.div(divisor: Number): Location = let {
    val d = divisor.toDouble()
    Location(world, x / d, y / d, z / d, yaw, pitch)
}

operator fun Location.plus(addend: Number): Location = let {
    val a = addend.toDouble()
    Location(world, x + a, y + a, z + a, yaw, pitch)
}

operator fun Location.minus(subtrahend: Number): Location = let {
    val s = subtrahend.toDouble()
    Location(world, x - s, y - s, z - s, yaw, pitch)
}

operator fun Vector.times(multiplicand: Number): Vector = let {
    val m = multiplicand.toDouble()
    Vector(x * m, y * m, z * m)
}

operator fun Vector.div(divisor: Number): Vector = let {
    val d = divisor.toDouble()
    Vector(x / d, y / d, z / d)
}

operator fun Vector.plus(addend: Number): Vector = let {
    val a = addend.toDouble()
    Vector(x + a, y + a, z + a)
}

operator fun Vector.minus(subtrahend: Number): Vector = let {
    val s = subtrahend.toDouble()
    Vector(x - s, y - s, z - s)
}

/* ================================
 * >> Player
 * ================================ */

fun Player.playSound(sound: Sound, location: Location = this.location, category: SoundCategory = SoundCategory.MASTER, volume: Float = 1F, pitch: Float = 1F) {
    playSound(location, sound, category, volume, pitch)
}
