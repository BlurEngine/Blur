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

package com.blurengine.blur.modules.checkpoints

import com.blurengine.blur.events.players.PlayerMoveBlockEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.modules.checkpoints.CheckpointsModule.CheckpointsData
import com.blurengine.blur.modules.goal.GoalWinnersEvent
import com.blurengine.blur.modules.stages.StageChangeReason
import com.blurengine.blur.serializers.ExtentList
import com.blurengine.blur.session.BlurPlayer
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import java.util.HashMap

@ModuleInfo(name = "BCheckpoints", dataClass = CheckpointsData::class)
class CheckpointsModule(manager: ModuleManager, val data: CheckpointsData) : Module(manager) {
    private val playerCheckpoint = HashMap<BlurPlayer, Int>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveBlockEvent) {
        val bp = event.blurPlayer
        var current = this.playerCheckpoint.getOrDefault(bp, 0)


        // Precaution in case this method gets fired before this listener unregisters.
        if (current < this.data.points.size) {
            val checkpoint = this.data.points[current]
            if (checkpoint.contains(bp)) {
                current++
                this.playerCheckpoint.put(bp, current)
                bp.message("${ChatColor.GREEN}Checkpoint reached, ${this.data.points.size - current} left")
            }
        }


        // player has reached the last point.        
        if (current >= this.data.points.size) {
            session.callEvent(GoalWinnersEvent(session, setOf<Any>(bp)))
            stagesManager.nextStage(StageChangeReason.OBJECTIVE_SUCCESS)
            return
        }
    }

    class CheckpointsData : ModuleData {
        val points: ExtentList = ExtentList()

        override fun parse(manager: ModuleManager, serialized: SerializedModule): Module? {
            serialized.load(this);
            check(points.isNotEmpty(), "At least one checkpoint must be defined.")
            return CheckpointsModule(manager, this)
        }
    }
}
