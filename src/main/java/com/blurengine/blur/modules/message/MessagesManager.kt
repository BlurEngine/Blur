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

package com.blurengine.blur.modules.message

import com.blurengine.blur.framework.InternalModule
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.utils.getMetadata
import com.blurengine.blur.utils.registerClassKt
import java.time.Duration
import java.time.Instant

@ModuleInfo(name = "Messages")
@InternalModule
class MessagesManager(moduleManager: ModuleManager) : Module(moduleManager) {
    private val _messages = HashMap<String, Message>()
    val messages: Map<String, Message> get() = _messages

    init {
        playerMetadataCreator.registerClassKt<PlayerMessageData>()
    }

    fun clearMessages() {
        _messages.clear()
        players.map { it.getMetadata<PlayerMessageData>()!! }.forEach { it.messagesCooldownMs.clear() }
    }
    fun isRegistered(message: Message) = _messages.containsValue(message)
    fun isRegistered(messageNode: String) = messageNode in _messages
    fun registerMessage(message: Message) = _messages.put(message.messageNode, message)
    fun unregisterMessage(messageNode: String) = _messages.remove(messageNode)

    operator fun get(messageNode: String) = getMessage(messageNode)

    fun getMessage(messageNode: String) = _messages[messageNode]

    fun isOnCooldown(blurPlayer: BlurPlayer, messageNode: String): Boolean {
        val lastMessage = blurPlayer.getMetadata<PlayerMessageData>()!!.messagesCooldownMs[messageNode]
        if (lastMessage != null && lastMessage.isAfter(Instant.now())) {
            return true
        }
        return false
    }

    fun sendMessage(blurPlayer: BlurPlayer, messageNode: String, vararg args: Any, cooldown: Duration? = null): Boolean {
        if (isOnCooldown(blurPlayer, messageNode)) {
            return false
        }
        val data = blurPlayer.getMetadata<PlayerMessageData>()!!
        val message = _messages[messageNode] ?: throw IllegalArgumentException("Unknown message node '$messageNode'")
        message.send(blurPlayer, *args)

        if (cooldown != null) {
            require(!cooldown.isNegative) { "cooldown must not be negative." }
            data.messagesCooldownMs[message.messageNode] = Instant.now().plus(cooldown)
        }
        return true
    }

    class PlayerMessageData {
        val messagesCooldownMs = HashMap<String, Instant>()
    }
}
