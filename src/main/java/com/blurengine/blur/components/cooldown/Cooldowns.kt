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

package com.blurengine.blur.components.cooldown

import com.blurengine.blur.events.players.PlayerLeaveSessionEvent
import com.blurengine.blur.framework.AbstractComponent
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.session.BlurPlayer
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import org.bukkit.event.EventHandler
import java.util.Collections

class CooldownsManager(moduleManager: ModuleManager) : AbstractComponent(moduleManager) {
    private val _cooldowns = HashMultimap.create<BlurPlayer, CooldownEntry>()
    val cooldowns: SetMultimap<BlurPlayer, CooldownEntry> get() = Multimaps.unmodifiableSetMultimap(_cooldowns)

    operator fun get(blurPlayer: BlurPlayer): Set<CooldownEntry> = Collections.unmodifiableSet(_cooldowns[blurPlayer])

    operator fun get(blurPlayer: BlurPlayer, cooldown: Cooldown<CooldownEntry>): CooldownEntry? = _cooldowns[blurPlayer].single { it.cooldown == cooldown }

    operator fun set(blurPlayer: BlurPlayer, cooldown: Cooldown<CooldownEntry>) = apply(blurPlayer, cooldown)

    fun <T : CooldownEntry> apply(blurPlayer: BlurPlayer, cooldown: Cooldown<T>): T {
        val ticks = cooldown.getCooldownFor(blurPlayer)
        val entry = cooldown.createEntry(this, blurPlayer, ticks)
        _cooldowns.put(blurPlayer, entry)
        return entry
    }

    fun <T : CooldownEntry> isOnCooldown(blurPlayer: BlurPlayer, cooldown: Cooldown<T>): Boolean {
        return _cooldowns.get(blurPlayer).any { it.cooldown == cooldown }
    }

    @Tick
    fun tick() {
        this._cooldowns.values().forEach { it.ticks-- }
        this._cooldowns.entries().filter { it.value.ticks <= 0 }.forEach {
            it.value.cooldown.onComplete(it.value)
            this._cooldowns.remove(it.key, it.value)
        }
    }

    @EventHandler
    fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
        if (isSession(event)) {
            this._cooldowns.removeAll(event.blurPlayer)
        }
    }

}

interface Cooldown<out T : CooldownEntry> {
    val cooldownTicks: Int

    fun createEntry(manager: CooldownsManager, blurPlayer: BlurPlayer, ticks: Int): T

    fun getCooldownFor(blurPlayer: BlurPlayer) = cooldownTicks

    fun tick(entry: @UnsafeVariance T) {}
    
    fun onComplete(entry: @UnsafeVariance T) {}
}

interface BasicCooldown : Cooldown<CooldownEntry> {
    override fun createEntry(manager: CooldownsManager, blurPlayer: BlurPlayer, ticks: Int)
            = CooldownEntry(manager, this, blurPlayer, ticks)
}

open class CooldownEntry(val manager: CooldownsManager, val cooldown: Cooldown<CooldownEntry>, val blurPlayer: BlurPlayer, ticks: Int) {
    internal var _ticks = ticks
    open var ticks: Int
        get() = _ticks
        set(value) {
            _ticks = value
            cooldown.tick(this)
        }

}

interface ExpCooldown : Cooldown<ExpCooldownEntry> {

    override fun createEntry(manager: CooldownsManager, blurPlayer: BlurPlayer, ticks: Int)
            = ExpCooldownEntry(manager, this, blurPlayer, ticks)

    override fun tick(entry: ExpCooldownEntry) {
        super.tick(entry)
        if (!entry.active) return

        val exp: Float
        if (!entry.increment) {
            exp = Math.min(entry.ticks * entry.incrementation, 1f)
        } else {
            exp = Math.max((entry.maxTicks - entry.ticks) * entry.incrementation, 0f)
        }
        entry.blurPlayer.player.exp = exp
    }
}


class ExpCooldownEntry(manager: CooldownsManager, cooldown: Cooldown<ExpCooldownEntry>, blurPlayer: BlurPlayer, ticks: Int)
    : CooldownEntry(manager, cooldown, blurPlayer, ticks) {

    var increment: Boolean = false
    var incrementation: Float = 1.0f / ticks
        private set
    var maxTicks: Int = ticks
        set(value) {
            field = value
            incrementation = 1.0f / ticks
        }

    override var ticks: Int
        get() = super.ticks
        set(value) {
            super.ticks = value
            if (maxTicks < ticks) {
                maxTicks = ticks
            }
        }

    private var _active: Boolean = false
    var active: Boolean
        get() = _active
        set(value) {
            // Deactivate all other ExpCooldownEntry
            for (entry in manager.cooldowns.values().filterIsInstance<ExpCooldownEntry>().filter { it != this }) {
                entry._active = false
            }
            _active = value
        }
}
