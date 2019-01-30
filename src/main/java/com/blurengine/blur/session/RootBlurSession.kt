/*
 * Copyright 2018 Ali Moghnieh
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

package com.blurengine.blur.session

import com.blurengine.blur.utils.toBlurPlayer
import com.google.common.base.Preconditions

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Represents a root [BlurSession] that is incharge of all the other [BlurSession]. There should only exist one instance of this class
 * across the whole program.
 */
class RootBlurSession(private val manager: SessionManager) : BlurSession(Preconditions.checkNotNull(manager, "manager cannot be null."), null), Listener {

    override fun load(): Boolean {
        if (super.load()) {
            manager.addSession(this)
            blur.plugin.registerEvents(this)
            return true
        }
        return false
    }

    /*
     * All players in the game are a part of RootBlurSession for encouraging compatible modules.
     */
    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val blurPlayer = event.player.toBlurPlayer()!!
        addPlayer(blurPlayer)
    }

    // Priority HIGH is crucial as the BlurPlayer reference is disposed of in HIGHEST
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val blurPlayer = blur.getPlayer(event.player) ?: return
        blurPlayer.isQuitting = true
        // recursively remove player from each session in the order child all the way up to greatest ancestor (RootBlurSession).
        var session = blurPlayer.session
        do {
            session.removePlayer(blurPlayer)

            session = session.parentSession
            if (session != null) {
                check(blurPlayer.session == session) { "blurPlayer session after removal is not parent." }
            }
        } while (session != null)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val blurPlayer = blur.getPlayer(event.entity)
        blurPlayer?.die()
    }
}
