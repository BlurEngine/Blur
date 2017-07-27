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

import com.blurengine.blur.framework.SharedComponent
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.session.BlurSession
import com.blurengine.blur.utils.isPositive
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.time.Duration
import java.time.Instant
import java.util.Collections
import java.util.WeakHashMap

class EntityRemover(session: BlurSession) : SharedComponent(session) {
    private val _expiringEntities = WeakHashMap<Entity, Instant>()
    val expiringEntities: Map<Entity, Instant> get() = Collections.unmodifiableMap(_expiringEntities)

    init {
        addListener(EntityListener())
    }

    fun schedule(entity: Entity, duration: Duration) {
        require(duration.isPositive()) { "Duration must be greater than 0" }
        _expiringEntities[entity] = Instant.now().plus(duration)
    }

    fun unschedule(entity: Entity): Boolean {
        return _expiringEntities.remove(entity) != null
    }

    fun clear(removeEntities: Boolean) {
        val it = _expiringEntities.keys.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (removeEntities) {
                next.remove()
            }
            it.remove()
        }
    }

    @Tick
    fun tick() {
        val it = _expiringEntities.entries.iterator()
        while (it.hasNext()) {
            val (entity, instant) = it.next()
            if (Instant.now() >= instant || entity.isDead) {
                entity.remove()
                it.remove()
            }
        }
    }

    private inner class EntityListener : Listener {
        @EventHandler
        fun onEntityDeath(event: EntityDeathEvent) {
            _expiringEntities.remove(event.entity)
        }

        @EventHandler
        fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
            if (event.entity is FallingBlock) {
                _expiringEntities.remove(event.entity)
            }
        }

        // TODO do we need to check for more events?
    }
}
