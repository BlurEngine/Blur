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
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.session.BlurSession
import com.blurengine.blur.utils.getSharedComponent
import com.blurengine.blur.utils.isPositive
import com.blurengine.blur.utils.toTicks
import com.supaham.commons.bukkit.utils.LocationUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import java.time.Duration

class EntityFreezer(session: BlurSession) : SharedComponent(session) {

    val NO_JUMP = BlurPotionEffects.NO_JUMP.infinite()
    val potionManager: PotionEffectManager get() = session.getSharedComponent { PotionEffectManager(session) }

    private val _frozenPlayers = HashMap<BlurPlayer, FreezePlayerData>()

    init {
        addListener(PlayerListener())
    }

    @Tick
    fun tick() {
        val it = _frozenPlayers.entries.iterator()
        while (it.hasNext()) {
            val (blurPlayer, data) = it.next()
            if (data.done) {
                unfreeze(blurPlayer, data)
                it.remove()
            }
        }
    }

    fun freeze(blurPlayer: BlurPlayer, duration: Duration, turningAllowed: Boolean) {
        val data = _frozenPlayers[blurPlayer]
        data?.apply {
            expiresTicks = duration.toTicks(session)
            this.turningAllowed = turningAllowed
            return
        }
        _frozenPlayers[blurPlayer] = FreezePlayerData(blurPlayer, duration, turningAllowed)
        potionManager.apply(blurPlayer.player, NO_JUMP)
        blurPlayer.player.apply {
            walkSpeed = 0f
            flySpeed = 0f
            allowFlight = true
            isFlying = true
        }
    }

    fun unfreeze(blurPlayer: BlurPlayer, data: FreezePlayerData? = _frozenPlayers.remove(blurPlayer)): Boolean {
        data?.apply {
            applyDefaults()
            potionManager.clear(blurPlayer.player, NO_JUMP.type)
            return true
        }
        return false
    }

    operator fun contains(blurPlayer: BlurPlayer) = _frozenPlayers.contains(blurPlayer)
    operator fun get(blurPlayer: BlurPlayer) = _frozenPlayers[blurPlayer]


    inner class PlayerListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerMove(event: PlayerMoveEvent) {
            val blurPlayer = getPlayer(event.player)
            if (!isSession(blurPlayer.session)) return
            val data = _frozenPlayers[blurPlayer] ?: return
            if (!data.turningAllowed || !LocationUtils.isSameCoordinates(event.from, event.to)) {
                val tp = event.from
                if (data.turningAllowed) {
                    tp.yaw = event.to!!.yaw
                    tp.pitch = event.to!!.pitch
                }
                event.player.teleport(tp)
            }
        }

        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            unfreeze(getPlayer(event.player))
        }

        @EventHandler
        fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
            if (!event.isFlying) {
                if(getPlayer(event.player) in this@EntityFreezer) {
                    event.isCancelled = true
                }
            }
        }

    }

    inner class FreezePlayerData(val blurPlayer: BlurPlayer, duration: Duration, var turningAllowed: Boolean) {
        val walkSpeed = blurPlayer.player.walkSpeed
        val flySpeed = blurPlayer.player.flySpeed
        val allowFlight = blurPlayer.player.allowFlight
        val isFlying = blurPlayer.player.isFlying

        var expiresTicks: Int
        val done: Boolean
            get() = expiresTicks <= 0

        fun applyDefaults() {
            blurPlayer.player.let { player ->
                player.walkSpeed = walkSpeed
                player.flySpeed = flySpeed
                player.allowFlight = allowFlight
                player.isFlying = isFlying
            }
        }

        init {
            require(duration.isPositive()) { "duration must be positive." }
            expiresTicks = duration.toTicks(session)
        }
    }
}
