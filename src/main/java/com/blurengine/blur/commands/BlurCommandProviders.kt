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

package com.blurengine.blur.commands

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.CommandContexts
import com.blurengine.blur.BlurPlugin
import com.blurengine.blur.session.BlurPlayer
import org.bukkit.entity.Player

class BlurCommandProviders(val plugin: BlurPlugin) {
    fun registerAll(context: CommandContexts<BukkitCommandExecutionContext>) {
        registerBlurPlayerSenderAwareContext(context)
    }

    fun registerBlurPlayerSenderAwareContext(context: CommandContexts<BukkitCommandExecutionContext>) {
        context.registerIssuerAwareContext(BlurPlayer::class.java) { _context ->
            val player = _context.sender as? Player ?: throw IllegalStateException("Only player command.")
            plugin.blur.getPlayer(player)
        }
    }
}
