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

package com.blurengine.blur.components.killstreak

import com.blurengine.blur.events.players.BlurPlayerDeathEvent
import com.blurengine.blur.events.players.BlurPlayerEvent
import com.blurengine.blur.events.players.PlayerKilledEvent
import com.blurengine.blur.framework.AbstractComponent
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.utils.getMetadata
import com.blurengine.blur.utils.registerClassKt
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.HandlerList
import java.time.Instant
import kotlin.properties.Delegates

class BasicPvpComponent(moduleManager: ModuleManager) : AbstractComponent(moduleManager) {

    init {
        playerMetadataCreator.registerClass(BasicPvpData::class.java) { BasicPvpData(it) } 
    }

    @EventHandler(priority = LOW)
    fun updateStatsOnDeath(event: BlurPlayerDeathEvent) {
        val data = event.blurPlayer.getMetadata<BasicPvpData>()!!
        data.notifyKillstreakChange(0)
        data.notifyDeathstreakChange(data.deathstreak + 1)
        data.lastDeathTime = Instant.now()
    }

    @EventHandler(priority = LOW)
    fun updateStatsOnKill(event: PlayerKilledEvent) {
        val data = event.killer.getMetadata<BasicPvpData>()!!
        data.notifyDeathstreakChange(0)
        data.notifyKillstreakChange(data.killstreak + 1)
        data.lastKillTime = Instant.now()
    }

    inner class BasicPvpData(val blurPlayer: BlurPlayer) {
        var deathstreak: Int = 0
        var lastDeathTime: Instant = Instant.MIN
        var killstreak: Int = 0
        var lastKillTime: Instant = Instant.MIN

        fun notifyKillstreakChange(killstreak: Int) {
            if (this.killstreak == killstreak) return
            this.killstreak = killstreak
            session.callEvent(KillstreakChangedEvent(this, this.killstreak))
        }

        fun notifyDeathstreakChange(deathstreak: Int) {
            if (this.deathstreak == deathstreak) return
            this.deathstreak = deathstreak
            session.callEvent(DeathstreakChangedEvent(this, this.deathstreak))
        }
    }
}

class KillstreakChangedEvent(
        val pvpData: BasicPvpComponent.BasicPvpData,
        val killstreak: Int
) : BlurPlayerEvent(pvpData.blurPlayer) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }
}

class DeathstreakChangedEvent(
        val pvpData: BasicPvpComponent.BasicPvpData,
        val deathstreak: Int
) : BlurPlayerEvent(pvpData.blurPlayer) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }
}
