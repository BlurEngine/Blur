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
    private val _entityEffects = HashBasedTable.create<UUID, PotionEffectType, PotionData>()

    init {
        addListener(EntityListener())
    }

    @Tick
    fun tick() {
        // Make copy of map to prevent CME when potionsIt removal causes _entityEffects key to be removed as well.
        val entityEffects = _entityEffects.rowMap().toMutableMap()
        entityEffects.forEach { (uuid, potionsMap) ->
            val entity = session.server.getEntity(uuid) as? LivingEntity

            val potionsIt = potionsMap.values.iterator()
            potionsIt.forEachRemaining {
                // Handle absence
                if (entity == null) {
                    if (!it.potion.sessionPersistent) {
                        potionsIt.remove()
                    }
                    return@forEachRemaining
                }
                it.expiresTicks--

                if (it.done) {
                    entity.removePotionEffect(it.potion.type)
                    potionsIt.remove()
                } else if (!entity.hasPotionEffect(it.potion.type)) {
                    it.apply(entity)
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
        val potions = _entityEffects.row(uuid).values
        if (entity != null) {
            potions.forEach {
                entity.removePotionEffect(it.potion.type)
            }
        }
        potions.clear()
    }

    fun clearAll() = _entityEffects.rowKeySet().forEach { e -> this.clearAll(e) }

    fun apply(entity: LivingEntity, potion: BlurPotionEffect, force: Boolean = false): Boolean {
        var force = force
        var potionData: PotionData
        if (!_entityEffects.contains(entity.uniqueId, potion.type)) {
            potionData = PotionData(potion.copy())
            _entityEffects.put(entity.uniqueId, potion.type, potionData)
        } else {
            potionData = _entityEffects.get(entity.uniqueId, potion.type)!!
            if (potionData.merge(potion)) {
                entity.removePotionEffect(potion.type)
                force = true
            }
        }
        return potionData.apply(entity, force)
    }

    fun getPotionData(entity: LivingEntity, potionEffectType: PotionEffectType) = getPotionData(entity.uniqueId, potionEffectType)
    fun getPotionData(entity: LivingEntity) = getPotionData(entity.uniqueId)

    fun getPotionData(uuid: UUID, potionEffectType: PotionEffectType): PotionData? {
        return _entityEffects.get(uuid, potionEffectType)
    }

    fun getPotionData(uuid: UUID): Set<PotionData> {
        return Collections.unmodifiableSet(HashSet(_entityEffects.row(uuid).values))
    }

    inner class EntityListener : Listener {

        @EventHandler
        fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
            val it = _entityEffects.row(event.blurPlayer.uuid).iterator()
            val player = event.blurPlayer.player
            while (it.hasNext()) {
                // Remove potion effects that are not session persistent.
                val data = it.next().value
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
            val it = _entityEffects.row(uuid).iterator()
            val entity = session.server.getEntity(uuid) as? LivingEntity
            while (it.hasNext()) {
                // Remove potion effects that are not death persistent.
                val data = it.next().value
                if (!data.potion.deathPersistent) {
                    entity?.removePotionEffect(data.potion.type)
                    it.remove()
                }
            }
        }
    }

    inner class PotionData(potion: BlurPotionEffect) {
        var potion: BlurPotionEffect = potion
            private set

        internal var expiresTicks: Int
        internal var lastApply = Instant.MIN
        val done: Boolean
            get() = expiresTicks <= 0

        init {
            require(potion.amplifier >= 0) { "amplifier cannot be less than 0." }
            require(potion.duration.isPositive()) { "duration must be positive." }
            expiresTicks = potion.duration.toTicks(session)
            if (expiresTicks < 0) {
                expiresTicks = Int.MAX_VALUE
            }
        }

        fun merge(p2: BlurPotionEffect): Boolean {
            require(p2.type == this.potion.type) { "Potion type mismatch during merge" }

            var change = false
            if (p2.amplifier > potion.amplifier) {
                potion = p2
                expiresTicks = p2.duration.toTicks(session)
                change = true
            } else if (p2.amplifier == potion.amplifier
                    && (Instant.now().plus(p2.duration) > lastApply.plus(potion.duration))) {
                // ^ consider lastApply because both potion durations are just that and will not be sufficient
                // to match the current live potion status (i.e. progress of expiresTicks)
                potion = p2
                expiresTicks = p2.duration.toTicks(session)
                change = true
            }
            if (p2.ambient != potion.ambient) {
                potion = potion.copy(ambient = p2.ambient)
                change = true
            }
            if (p2.particles != potion.particles) {
                potion = potion.copy(ambient = p2.particles)
                change = true
            }
            if (expiresTicks < 0) {
                expiresTicks = Int.MAX_VALUE
            }
            return change
        }

        fun apply(entity: LivingEntity, force: Boolean = false): Boolean {
            val applied: Boolean
            if (force || (potion.reapplyDuration.isPositive() && lastApply.elapsed(potion.reapplyDuration))) {
                applied = entity.addPotionEffect(createPotionEffect(), true)
                lastApply = Instant.now()
            } else {
                applied = entity.addPotionEffect(createPotionEffect(), false)
                if (applied) {
                    lastApply = Instant.now()
                }
            }
            return applied
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
    val NO_JUMP = BlurPotionEffect(PotionEffectType.JUMP, 128, Duration.ofSeconds(30), particles = false)
    val NO_WALK = BlurPotionEffect(PotionEffectType.SLOW, 6, Duration.ofSeconds(30), particles = false)
    val INFINITE_INVISIBLITY = BlurPotionEffect(PotionEffectType.SLOW, 6, Duration.ofSeconds(30))
}
