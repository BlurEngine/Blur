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

package com.blurengine.blur.components.shared

import com.blurengine.blur.events.players.BlurPlayerDeathEvent
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent
import com.blurengine.blur.framework.SharedComponent
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.session.BlurSession
import com.blurengine.blur.utils.elapsed
import com.blurengine.blur.utils.isPositive
import com.blurengine.blur.utils.toTicks
import com.google.common.collect.HashBasedTable
import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration
import java.time.Instant
import java.util.Collections
import java.util.UUID

class PotionEffectManager(session: BlurSession) : SharedComponent(session) {
    private val _entityEffects = LinkedHashMultimap.create<UUID, PotionData>()

    init {
        addListener(EntityListener())
    }

    @Tick
    fun tick() {
        val entriesIterator = _entityEffects.keys().iterator()
        while (entriesIterator.hasNext()) {
            val uuid = entriesIterator.next()
            val dataIterator = _entityEffects[uuid].iterator()
            val entity = session.server.getEntity(uuid) as? LivingEntity
            val effectsToRemove = if (entity != null) HashSet<PotionEffectType>() else Collections.emptySet<PotionEffectType>()
            while (dataIterator.hasNext()) {
                val data = dataIterator.next()
                // Handle absence
                if (entity == null) {
                    if (!data.potion.sessionPersistent) {
                        dataIterator.remove()
                        continue
                    }
                }

                data.expiresTicks--
                if (data.done) {
                    dataIterator.remove()
                    effectsToRemove.add(data.potion.type)
                } else {
                    if (entity != null && !entity.hasPotionEffect(data.potion.type)) {
                        data.apply(entity)
                    }
                }
            }
            if (entity != null) {
                for (type in effectsToRemove) {
                    val otherPotion = _entityEffects.get(uuid).filter { it.potion.type == type }.firstOrNull()
                    if (otherPotion != null) {
                        otherPotion.apply(entity)
                    } else {
                        entity.removePotionEffect(type)
                    }
                }
            }
        }
    }

    fun clear(entity: LivingEntity, potionEffectType: PotionEffectType): Boolean {
        _entityEffects.remove(entity.uniqueId, potionEffectType) ?: return false
        entity.removePotionEffect(potionEffectType)
        return true
    }

    fun clear(uuid: UUID, potionEffectType: PotionEffectType): Boolean {
        _entityEffects.remove(uuid, potionEffectType) ?: return false
        val entity = session.server.getEntity(uuid) as? LivingEntity ?: return false
        entity.removePotionEffect(potionEffectType)
        return true
    }

    fun clearAll(uuid: UUID) {
        val entity = session.server.getEntity(uuid) as? LivingEntity
        if (entity == null) {
            _entityEffects.removeAll(uuid)
            return
        }
        val it = _entityEffects[uuid].iterator()
        while (it.hasNext()) {
            val data = it.next()
            entity.removePotionEffect(data.potion.type)
            it.remove()
        }
    }
    
    fun clearAll() = _entityEffects.keySet().forEach(this::clearAll)

    fun apply(entity: LivingEntity, potion: BlurPotionEffect, force: Boolean = false) {
        val potionData = PotionData(potion.copy())
        _entityEffects.put(entity.uniqueId, potionData)
        potionData.apply(entity, force)
    }

    fun getPotionData(entity: LivingEntity, potionEffectType: PotionEffectType) = getPotionData(entity.uniqueId, potionEffectType)
    fun getPotionData(entity: LivingEntity) = getPotionData(entity.uniqueId)

    fun getPotionData(uuid: UUID, potionEffectType: PotionEffectType): Set<PotionData> {
        return Collections.unmodifiableSet(_entityEffects.get(uuid).filter { it.potion.type == potionEffectType }.toSet())
    }

    fun getPotionData(uuid: UUID): Set<PotionData> {
        return Collections.unmodifiableSet(_entityEffects.get(uuid))
    }

    inner class EntityListener : Listener {

        @EventHandler
        fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
            val it = _entityEffects[event.blurPlayer.uuid].iterator()
            val player = event.blurPlayer.player
            while (it.hasNext()) {
                // Remove potion effects that are not death persistent.
                val data = it.next()
                player.removePotionEffect(data.potion.type) // remove all effects
                if (!data.potion.sessionPersistent) {
                    it.remove()
                }
            }

        }

        @EventHandler
        fun onEntityDeath(event: EntityDeathEvent) {
            handleDeath(event.entity.uniqueId)
        }

        @EventHandler
        fun onBlurPlayerDeath(event: BlurPlayerDeathEvent) {
            handleDeath(event.blurPlayer.uuid)
        }

        private fun handleDeath(uuid: UUID) {
            val it = _entityEffects[uuid].iterator()
            while (it.hasNext()) {
                // Remove potion effects that are not death persistent.
                val data = it.next()
                if (!data.potion.deathPersistent) {
                    val entity = session.server.getEntity(uuid) as? LivingEntity
                    entity?.removePotionEffect(data.potion.type)
                    it.remove()
                }
            }
        }
    }

    inner class PotionData(val potion: BlurPotionEffect) {
        internal var expiresTicks: Int
        internal var lastApply = Instant.MIN
        val done: Boolean
            get() = expiresTicks <= 0

        init {
            require(potion.amplifier >= 0) { "amplifier cannot be less than 0." }
            require(potion.duration.isPositive()) { "duration must be positive." }
            expiresTicks = potion.duration.toTicks(session)
            if (potion.hidingDuration || expiresTicks < 0) expiresTicks = Int.MAX_VALUE
        }

        fun apply(entity: LivingEntity, force: Boolean = false) {
            if (force || (lastApply.elapsed(potion.reapplyDuration))) {
                entity.addPotionEffect(createPotionEffect(), true)
                lastApply = Instant.now()
            } else {
                if (entity.addPotionEffect(createPotionEffect(), false)) {
                    lastApply = Instant.now()
                }
            }
        }

        internal fun createPotionEffect() = PotionEffect(potion.type, expiresTicks, potion.amplifier, potion.ambient, potion.particles)
    }
}

data class BlurPotionEffect(val type: PotionEffectType, val amplifier: Int, val duration: Duration, val ambient: Boolean = false,
                            val particles: Boolean = true, val reapplyDuration: Duration = Duration.ZERO, val hidingDuration: Boolean = false,
                            val deathPersistent: Boolean = false, val sessionPersistent: Boolean = false) {
    fun infinite() = copy(duration = Duration.ofMillis(Long.MAX_VALUE))
}

object BlurPotionEffects {
    val NO_JUMP = BlurPotionEffect(PotionEffectType.JUMP, 128, Duration.ofSeconds(30))
    val NO_WALK = BlurPotionEffect(PotionEffectType.SLOW, 6, Duration.ofSeconds(30))
    val INFINITE_INVISIBLITY = BlurPotionEffect(PotionEffectType.SLOW, 6, Duration.ofSeconds(30))
}
