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

import com.blurengine.blur.BlurPlugin
import com.blurengine.blur.framework.Component
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.metadata.MetadataHolder
import com.blurengine.blur.framework.metadata.auto.AbstractAutoMetadataCreator
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.session.BlurSession
import com.supaham.commons.bukkit.utils.ImmutableVector
import com.supaham.commons.bukkit.utils.RelativeVector
import com.supaham.commons.minecraft.world.space.Position
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount

fun Instant.elapsed(temporalAmount: TemporalAmount) = Instant.now().minus(temporalAmount) >= this

fun Duration.isPositive(): Boolean = this.seconds > 0 || this.nano > 0

/* ================================
 * >> Space
 * ================================ */

fun Vector.toPosition(yaw: Float = 0F, pitch: Float = 0F) = Position(x, y, z, yaw, pitch)

fun ImmutableVector.toPosition(yaw: Float = 0F, pitch: Float = 0F) = Position(x, y, z, yaw, pitch)

fun Location.toPosition() = Position(x, y, z, yaw, pitch)

fun Location.toSupaVector() = com.supaham.commons.minecraft.world.space.Vector(x, y, z)

fun Position.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)

/* ================================
 * >> Framework
 * ================================ */

inline fun <reified T : Any> AbstractAutoMetadataCreator<*>.registerClassKt() = this.registerClass(T::class.java)

inline fun <reified T : Any> MetadataHolder.getMetadata(): T? = this.getMetadata(T::class.java)

inline fun <reified T : Component> BlurSession.getSharedComponent(): T? = this.getSharedComponent(T::class.java)

/**
 * Returns or creates (and puts) shared [Component].
 */
inline fun <reified T : Component> BlurSession.getSharedComponent(crossinline create: () -> T): T {
    var found = this.getSharedComponent(T::class.java)

    if (found == null) {
        found = create()
        this.putSharedComponent(found)
    }
    return found
}

/* ================================
 * >> Player
 * ================================ */

fun Player.toBlurPlayer() = BlurPlugin.get().blur.getPlayer(this)

fun BlurPlayer.getTeam() = session.moduleManager.teamManager.getPlayerTeam(this)

fun Player.playSound(sound: Sound, location: Location = this.location, category: SoundCategory = SoundCategory.MASTER, volume: Float = 1F, pitch: Float = 1F) {
    playSound(location, sound, category, volume, pitch)
}

inline fun <reified T : Module> BlurSession.getModule(): List<T> = this.getModule(T::class.java)

fun RelativeVector.withMultiply(vector: Vector): Vector {
    val _vector = vector.clone()
    _vector.x = if (this.isXRelative) _vector.x * x else x
    _vector.y = if (this.isYRelative) _vector.y * y else y
    _vector.z = if (this.isZRelative) _vector.z * z else z
    return _vector
}

fun World.spawnParticleKt(particle: Particle, location: Location, count: Int = 1, offX: Number = 0, offY: Number = 0, offZ: Number = 0,
                              extra: Number = 1, data: Any? = null)
        = spawnParticle(particle, location, count, offX.toDouble(), offY.toDouble(), offZ.toDouble(), extra.toDouble(), data)

fun Location.add(x: Number = 0, y: Number = 0, z: Number = 0): Location = add(x.toDouble(), y.toDouble(), z.toDouble())
