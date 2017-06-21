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

package com.blurengine.blur.modules.misc

import com.blurengine.blur.events.players.PlayerMoveBlockEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.misc.JumpPadsModule.JumpPadsModuleData
import com.blurengine.blur.utils.withMultiply
import com.supaham.commons.bukkit.utils.RelativeVector
import org.bukkit.event.EventHandler
import pluginbase.config.annotation.Name

@ModuleInfo(name = "JumpPads", dataClass = JumpPadsModuleData::class)
class JumpPadsModule(moduleManager: ModuleManager, val data: JumpPadsModuleData) : Module(moduleManager) {

    @EventHandler
    fun onPlayerMoveBlock(event: PlayerMoveBlockEvent) {
        for (jumpPad in data.jumpPads) {
            if (jumpPad.extent.contains(event.blurPlayer)) {
                val dir = event.blurPlayer.player.location.direction
                val jumpPadData = jumpPad.velocity ?: data.velocity!!
                event.blurPlayer.player.velocity = jumpPadData.withMultiply(dir)
            }
        }
    }

    class JumpPadsModuleData : CommonJumpPadData(), ModuleData {
        @Name("jump-pads")
        var jumpPads = ArrayList<JumpPadEntry>()

        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            // Defaults of CommonJumpPadData
            this.velocity = RelativeVector(1.0, 1.4, 1.0, true, false, true) // 1,~1.4,1

            serialized.load(this)

            for (jumpPad in jumpPads) {
                checkNotNullLateInit({ jumpPad.extent }, "JumpPad location must be set")
            }
            return JumpPadsModule(moduleManager, this)
        }
    }

    open class CommonJumpPadData {
        @Name("velocity")
        var velocity: RelativeVector? = null
    }

    class JumpPadEntry : CommonJumpPadData() {
        lateinit var extent: Extent
    }
}
