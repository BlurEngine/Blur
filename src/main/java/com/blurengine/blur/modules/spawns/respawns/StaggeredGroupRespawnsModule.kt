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
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.spawns.SpawnsModule
import com.blurengine.blur.modules.spawns.respawns.StaggeredGroupRespawnsModule.StaggeredGroupRespawnsData
import com.blurengine.blur.modules.teams.BlurTeam
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.utils.getModule
import com.blurengine.blur.utils.getTeam
import com.google.common.collect.HashMultimap
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import pluginbase.config.annotation.Name
import java.time.Duration
import java.util.WeakHashMap

@ModuleInfo(name = "StaggeredGroupRespawns", dataClass = StaggeredGroupRespawnsData::class)
class StaggeredGroupRespawnsModule(moduleManager: ModuleManager, val data: StaggeredGroupRespawnsData) : WorldModule(moduleManager) {

    val theDead = WeakHashMap<BlurPlayer, Long>()
    private val spawnerBossBar by lazy { SpawnerBossBar() }

    override fun load() {
        super.load()
        // Disable SpawnsModule join handling.
        session.getModule<SpawnsModule>().firstOrNull()?.let {
            logger.fine { "Disabling late join in StaggeredGroupRespawns" }
            it.data.handleLateJoinSpawn = false
        }
    }

    override fun disable() {
        super.disable()
        theDead.keys.toMutableSet().forEach { destroyPlayer(it) }
    }

    fun sendToDeathbox(blurPlayer: BlurPlayer) {
        theDead[blurPlayer] = System.currentTimeMillis()
        blurPlayer.coreData.isAlive = false
        if (data.teleportTo != null) {
            blurPlayer.player.leaveVehicle()
            blurPlayer.player.teleport(data.teleportTo!!.randomLocation.toLocation(world))
        }
        if (data.useBossBar) {
            spawnerBossBar.add(blurPlayer)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onBlurPlayerDeath(event: BlurPlayerDeathEvent) {
        if (isSession(event)) {
            sendToDeathbox(event.blurPlayer)
        }
    }

    @EventHandler
    fun onBlurPlayerRespawn(event: BlurPlayerRespawnEvent) {
        if (isSession(event)) {
            destroyPlayer(event.blurPlayer)
        }
    }

    private fun destroyPlayer(blurPlayer: BlurPlayer) {
        theDead.remove(blurPlayer)
        spawnerBossBar.remove(blurPlayer)
    }

    @EventHandler
    fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
        if (isSession(event.session)) {
            destroyPlayer(event.blurPlayer)
        }
    }

    @Tick
    fun ticker() {
        if (theDead.isEmpty()) {
            return
        }

        val theDeadTeams = HashMultimap.create<BlurTeam, BlurPlayer>()

        // Remove invalid/offline players
        val it = theDead.keys.iterator()
        for (blurPlayer in it) {
            if (data.deathbox?.contains(blurPlayer) == false) {
                sendToDeathbox(blurPlayer)
            }

            if (!session.players.values.any { it.session == session }) {
                it.remove()
                continue
            }
            if (!blurPlayer.player.isValid) {
                destroyPlayer(blurPlayer)
            } else {
                theDeadTeams.put(blurPlayer.getTeam(), blurPlayer)
            }
        }

        theDeadTeams.asMap().forEach { _, players ->
            val playerDeadTimes = players.associate { it to (System.currentTimeMillis() - theDead[it]!!) }.entries
            val passedMinWait = playerDeadTimes.filter { it.value >= data.minTimer.toMillis() }

            // Check if enough players waited minimum duration  
            if (passedMinWait.size >= data.minPlayers) {
                if (logger.debugLevel >= 2) {
                    logger.finer("Enough players to spawn players: ${passedMinWait.map { it.key.name }}")
                }
                passedMinWait.forEach {
                    it.key.respawn()
                }
            } else {
                val needToSpawn = passedMinWait.filter { it.value >= data.maxTimer.toMillis() }
                if (needToSpawn.isNotEmpty() && logger.debugLevel >= 2) {
                    logger.finer("${needToSpawn.map { it.key.name }} waited ${data.maxTimer.seconds}s.")
                }
                needToSpawn.forEach {
                    it.key.respawn()
                }
            }
        }

        if (data.useBossBar) {
            spawnerBossBar.ticker()
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
        var deathbox: Extent? = null
        @Name("use-boss-bar")
        var useBossBar: Boolean = true

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

    private inner class SpawnerBossBar {
        private val _bossBars = HashMap<BlurPlayer, BossBar>()

        fun ticker() {
            val bossBars = HashMap(_bossBars)
            bossBars.forEach { blurPlayer, bossBar ->
                if (blurPlayer !in theDead) {
                    remove(blurPlayer)
                    return@forEach
                }
                val deathTimeMs = (System.currentTimeMillis() - theDead[blurPlayer]!!)
                val deathTimeRemaining = data.maxTimer.toMillis() - deathTimeMs
                var progress = 1.0 - (deathTimeMs / data.maxTimer.toMillis().toDouble())
                progress = Math.max(0.0, Math.min(progress, 1.0))
                val color: BarColor
                var title = when {
                    progress > 2 / 3.0 -> {
                        color = BarColor.GREEN
                        "${ChatColor.GREEN}Respawning soon..."
                    }
                    progress > 1 / 3.0 -> {
                        color = BarColor.YELLOW
                        "${ChatColor.GOLD}Waiting for respawn group..."
                    }
                    else -> {
                        color = BarColor.RED
                        "${ChatColor.RED}Respawn imminent!"
                    }
                }
                title += "    ${DurationFormatUtils.formatDuration(deathTimeRemaining, "ss.", false)}"
                title += "${(deathTimeRemaining % 1000).toString()[0]}s"
                bossBar.title = title
                bossBar.progress = progress
                if (bossBar.color != color) {
                    bossBar.color = color
                }
            }
        }

        fun add(blurPlayer: BlurPlayer): BossBar {
            check(data.useBossBar) { "use-boss-bar disabled" }
            val bar: BossBar = if (blurPlayer in _bossBars) {
                _bossBars[blurPlayer]!!
            } else {
                Bukkit.createBossBar("TITLE NOT SET", BarColor.PURPLE, BarStyle.SOLID)
                        .apply { _bossBars.put(blurPlayer, this) }
            }
            bar.progress = 1.0
            bar.addPlayer(blurPlayer.player)
            return bar
        }

        fun remove(blurPlayer: BlurPlayer): BossBar? {
            return _bossBars.remove(blurPlayer)?.apply {
                this.removePlayer(blurPlayer.player)
            }
        }
    }
}
