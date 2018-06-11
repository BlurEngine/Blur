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

package com.blurengine.blur.modules.lobby

import com.blurengine.blur.countdown.AbstractCountdown
import com.blurengine.blur.countdown.GlobalGameCountdown
import com.blurengine.blur.events.players.PlayerJoinSessionEvent
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent
import com.blurengine.blur.events.session.BlurSessionEvent
import com.blurengine.blur.events.session.SessionStopEvent
import com.blurengine.blur.framework.ComponentState
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.ModuleParseException
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.modules.lobby.LobbyModule.LobbyData
import com.blurengine.blur.modules.maploading.MapLoadException
import com.blurengine.blur.modules.maploading.MapLoaderModule
import com.blurengine.blur.modules.maploading.MapLoaderPreLoadEvent
import com.blurengine.blur.modules.spawns.SpawnsModule
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.session.BlurSession
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import pluginbase.config.annotation.Name
import java.time.Duration
import java.util.ArrayList

@ModuleInfo(name = "Lobby", dataClass = LobbyData::class)
class LobbyModule(moduleManager: ModuleManager, private val data: LobbyData) : WorldModule(moduleManager) {
    private val childrenSessions = ArrayList<BlurSession>()
    private var countdown: AbstractCountdown? = null

    init {
        this.countdown = LobbyCountdown()
    }

    @EventHandler
    fun onMapLoaderPreLoad(event: MapLoaderPreLoadEvent) {
        // Cancel any initial MapLoaderModule loading events since we handle it in LobbyCountdown.
        if (isSession(event.module.session)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (data.games == 1) {
            val blurPlayer = session.blur.getPlayer(event.player)
            session.addPlayer(blurPlayer)
        } else {
            logger.warning("LobbyModule can't handle more than one game at a time yet.")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val blurPlayer = session.blur.getPlayer(event.player)
        if (players.contains(blurPlayer)) {
            session.removePlayer(blurPlayer, true)
        }
    }

    @EventHandler
    fun onPlayerJoinSession(event: PlayerJoinSessionEvent) {
        if (isSession(event)) {
            event.blurPlayer.reset()
            if (this.childrenSessions.isEmpty()) {
                checkAndStart()
            } else if (this.childrenSessions.first().state == ComponentState.ENABLED) {
                this.childrenSessions.first().addPlayer(event.blurPlayer)
            }
        }
    }

    @EventHandler
    fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
        if (isSession(event)) {
            if (this.countdown != null) {
                this.countdown!!.stop()
            }
        }
    }

    /*
     * Teleport players to the lobby when a session created by this lobby is shutdown.
     */
    @EventHandler
    fun onSessionStop(event: SessionStopEvent) {
        if (session.childrenSessions.contains(event.session)) {
            val spawns = session.getModule(SpawnsModule::class.java)[0]
            event.session.players.values.forEach(spawns::spawnPlayer)

            // Start countdown immediately.
            event.session.addOnStopTask {
                this@LobbyModule.childrenSessions.remove(event.session)
                this@LobbyModule.checkAndStart()
            }
        }
    }

    fun checkAndStart() {
        if (session.players.size >= data.requiredPlayers) {

            check(this.childrenSessions.isEmpty()) { "LobbyModule only supports 1 session at a time." }
            if (this.countdown != null) {
                if (this.countdown!!.state !== ComponentState.ENABLED) {
                    this.countdown!!.start()
                }
            } else {
                startNextSession()
            }
        }
    }

    private fun startNextSession() {
        if (this.countdown != null) {
            this.countdown!!.stop()
        }

        val mapLoaderModule = moduleManager.getModule(MapLoaderModule::class.java)[0] // FIXME this is a temporary hack
        try {
            var players: MutableList<BlurPlayer> = ArrayList(session.players.values)
            players = session.callEvent(LobbyPrepareSessionEvent(this, players)).players
            val childSession = mapLoaderModule.createSessionFromDirectory(mapLoaderModule.nextMap())

            // Make the wheels on the bus go round and round.
            childSession.load()
            childSession.enable()

            // Add current lobby players to the new session immediately
            players.forEach(childSession::addPlayer)

            if (!data.delay.isZero) {
                newUnregisteredTask { childSession.start() }.delay(data.delay).build()
            } else {
                childSession.start()
            }
            this.childrenSessions.add(childSession)
        } catch (e: MapLoadException) {
            e.printStackTrace()
            session.stop() // Stop session because of the map load failure.
        }

    }

    class LobbyData : ModuleData {

        val countdown: Duration = Duration.ofSeconds(15)
        @Name("delay-start-session")
        val delay: Duration = Duration.ZERO

        @Name("required-players")
        val requiredPlayers = 1
        val games = 1

        @Throws(ModuleParseException::class)
        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            serialized.load(this)
            check(games == 1, "Lobby only supports games=1 at the moment. Sorry :(")
            return LobbyModule(moduleManager, this)
        }
    }

    private inner class LobbyCountdown : GlobalGameCountdown(this@LobbyModule, Math.max(1, (data.countdown.toMillis() / 50).toInt())) {

        override fun onEnd() {
            super.onEnd()
            startNextSession()
        }
    }
}

class LobbyPrepareSessionEvent(val lobby: LobbyModule, val players: MutableList<BlurPlayer>) : BlurSessionEvent(lobby.session) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }
}

