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
import com.blurengine.blur.framework.SharedComponent
import com.blurengine.blur.framework.metadata.MetadataHolder
import com.blurengine.blur.framework.metadata.auto.AbstractAutoMetadataCreator
import com.blurengine.blur.framework.metadata.auto.MetadataCreator
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.session.BlurSession
import com.blurengine.blur.text.TextFormatter
import com.supaham.commons.bukkit.utils.ImmutableVector
import com.supaham.commons.bukkit.utils.RelativeVector
import com.supaham.commons.minecraft.world.space.Position
import net.kyori.text.TextComponent
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
 * >> Text
 * ================================ */

fun net.kyori.text.Component.format(vararg args: Any?): net.kyori.text.Component = TextFormatter.format(this, args)

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

inline fun <reified T : Any, M: MetadataHolder> AbstractAutoMetadataCreator<M>.registerClassKt(noinline creator: (M) -> T) {
    val metadataCreator = MetadataCreator(creator)
    return this.registerClass(T::class.java, metadataCreator)
}

inline fun <reified T : Any> MetadataHolder.getMetadata(): T? = this.getMetadata(T::class.java)

inline fun <reified T : SharedComponent> BlurSession.getSharedComponent(): T? = this.getSharedComponent(T::class.java)

/**
 * Returns or creates (and puts) shared [Component].
 */
inline fun <reified T : SharedComponent> BlurSession.getSharedComponent(crossinline create: () -> T): T {
    var found = this.getSharedComponent(T::class.java)

    if (found == null) {
        found = create()
        this.putSharedComponent(found)
    }
    return found
}

fun BlurSession.tl(messageNode: String): net.kyori.text.Component {
    return moduleManager.messagesManager[messageNode]!!.component
}

fun net.kyori.text.TextComponent.appendKt(index: Int, component: net.kyori.text.TextComponent): net.kyori.text.TextComponent {
    this.detectCycle(component)
    // -1 begins to define the component parameter as the new parent. Then, the receiver and its children become direct descendants of the builder 
    when {
        index == -1 -> {
            val builder = TextComponent.builder()
            builder.mergeStyle(this)
            builder.mergeStyle(component)
            builder.content(component.content())
            this.resetStyle()
            // Append old parent, and only itself, as a child of builder.
            // Then, add the old parent's children as siblings alongside it in the builder.
            builder.append(TextComponent.builder(this.content()).mergeStyle(this).build())
            builder.append(this.children())
            return builder.build()
        }
        index > 0 -> {
            return component.copy().apply { this@apply.children().add(index, component) }
        }
        else -> throw IndexOutOfBoundsException("index must be positive or -1")
    }
}

fun Duration.toTicks(session: BlurSession) = session.millisecondsToTicks(toMillis())

/* ================================
 * >> Player
 * ================================ */

fun Player.toBlurPlayer() = BlurPlugin.get().blur.getPlayer(this)

fun BlurPlayer.getTeam() = session.moduleManager.teamManager.getPlayerTeam(this)

inline fun <reified T : Module> BlurSession.getModule(): List<T> = this.getModule(T::class.java)

/**
 * Returns list of modules while climbing through session hierarchy.
 */
inline fun <reified T : Module> BlurSession.getModuleClimbing(): List<T> {
    val modules = this.getModule(T::class.java)
    var parent = this.parentSession
    while (parent != null) {
        modules += parent.getModule(T::class.java)
        parent = parent.parentSession
    }
    return modules
}

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
