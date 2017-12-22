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

package com.blurengine.blur.events.players

import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.session.BlurSession
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

/**
 * [PlayerSwitchSessionEvent] is called when a [BlurPlayer] is switching from one session to another.
 * When [RootBlurSession] is involved, the event is never fired.
 */
class PlayerSwitchSessionEvent(blurPlayer: BlurPlayer, var nextSession: BlurSession?) : BlurPlayerEvent(blurPlayer), Cancellable {
    val oldSession: BlurSession = blurPlayer.session

    private var cancelled = false
    override fun isCancelled() = cancelled
    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
