/*
 * Copyright 2016 Ali Moghnieh
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

package com.blurengine.blur.modules.spawns.respawns

import com.blurengine.blur.events.players.BlurPlayerDeathEvent
import com.blurengine.blur.events.players.BlurPlayerRespawnEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.spawns.respawns.StaggeredGroupRespawnsModule.StaggeredGroupRespawnsData
import com.blurengine.blur.modules.teams.BlurTeam
import com.blurengine.blur.session.BlurPlayer
import com.google.common.collect.HashMultimap
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import pluginbase.config.annotation.Name
import java.time.Duration
import java.util.WeakHashMap

@ModuleInfo(name = "StaggeredGroupRespawns", dataClass = StaggeredGroupRespawnsData::class)
class StaggeredGroupRespawnsModule(moduleManager: ModuleManager, val data: StaggeredGroupRespawnsData) : WorldModule(moduleManager) {

    val theDead = WeakHashMap<BlurPlayer, Long>()

    @EventHandler(priority = EventPriority.LOW)
    fun onBlurPlayerDeath(event: BlurPlayerDeathEvent) {
        if (isSession(event)) {
            theDead[event.blurPlayer] = System.currentTimeMillis()
            if (data.teleportTo != null) {
                event.blurPlayer.player.teleport(data.teleportTo!!.randomLocation.toLocation(world))
            }
        }
    }

    @EventHandler
    fun onBlurPlayerRespawn(event: BlurPlayerRespawnEvent) {
        if (isSession(event)) {
            theDead.remove(event.blurPlayer)
        }
    }

    @Tick
    fun ticker() {
        val theDeadTeams = HashMultimap.create<BlurTeam, BlurPlayer>()
        theDead.keys.forEach { theDeadTeams.put(teamManager.getPlayerTeam(it), it) }
        theDeadTeams.asMap().forEach { _, players ->
            val playerDeadTimes = players.associate { it to (System.currentTimeMillis() - theDead[it]!!) }.entries
            val passedMinWait = playerDeadTimes.filter { it.value >= data.minTimer.toMillis() }

            if (passedMinWait.size >= data.minPlayers) {
                passedMinWait.forEach {
                    if (logger.debugLevel >= 2) {
                        logger.finer("Enough players to spawn players: ${passedMinWait.map { it.key.name }}")
                    }
                    it.key.respawn()
                }
            } else {
                passedMinWait.filter { it.value >= data.maxTimer.toMillis() }.forEach {
                    logger.finer("${it.key.name} waited ${data.maxTimer.seconds}s.")
                    it.key.respawn()
                }
            }
        }
    }

    class StaggeredGroupRespawnsData : ModuleData {

        @Name("min-timer")
        lateinit var minTimer: Duration
        @Name("max-timer")
        lateinit var maxTimer: Duration
        @Name("min-players")
        var minPlayers: Int = 0
        @Name("teleport-to")
        var teleportTo: Extent? = null

        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            serialized.load(this)
            checkNotNullLateInit({ minTimer }, "min-timer")
            checkNotNullLateInit({ maxTimer }, "max-timer")
            check(!minTimer.isNegative && !minTimer.isZero) { "min-timer must be positive." }
            check(!maxTimer.isNegative && !maxTimer.isZero) { "max-timer must be positive." }
            check(minPlayers > 0) { "min-players must be greater than 0." }
            return StaggeredGroupRespawnsModule(moduleManager, this)
        }
    }
}
