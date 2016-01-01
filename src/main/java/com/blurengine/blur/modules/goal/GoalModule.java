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

package com.blurengine.blur.modules.goal;

import com.blurengine.blur.events.players.PlayerDeathEvent;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.stages.StageChangeReason;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.modules.goal.GoalModule.GoalModuleData;
import com.supaham.commons.utils.DurationUtils;

import org.bukkit.event.EventHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import pluginbase.config.annotation.Name;

@ModuleInfo(name = "Goal", dataClass = GoalModuleData.class)
public class GoalModule extends Module {

    private final Duration timeLimit;
    private final int lives;

    private Map<BlurPlayer, Integer> deaths = new HashMap<>();

    public GoalModule(ModuleManager moduleManager, GoalModuleData data) {
        super(moduleManager);
        this.timeLimit = data.timeLimit;
        this.lives = data.lives;

        newTask(this::timeIsUp).delay(timeLimit).build();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (lives > 0) {
            BlurPlayer bp = event.getBlurPlayer();
            Integer deaths = this.deaths.getOrDefault(bp, 0);
            this.deaths.put(bp, ++deaths);
            if (deaths >= lives) {
                // TODO set player team.
            }
        }
    }

    private void timeIsUp() {
        getLogger().fine("GameModule time limit of " + DurationUtils.toString(timeLimit, true) + " reached.");
        getStagesManager().nextStage(StageChangeReason.TIME_LIMIT);
    }

    public static final class GoalModuleData implements ModuleData {

        @Name("time-limit")
        private Duration timeLimit;
        private int lives;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new GoalModule(moduleManager, this);
        }
    }
}
