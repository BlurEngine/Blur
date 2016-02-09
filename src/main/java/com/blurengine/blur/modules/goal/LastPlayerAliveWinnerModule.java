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
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.WorldModule;
import com.blurengine.blur.modules.goal.Winner.PlayerWinner;
import com.blurengine.blur.modules.stages.StageChangeReason;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession.Predicates;

import org.bukkit.event.EventHandler;

import java.util.Collections;
import java.util.List;

@ModuleInfo(name = "LastPlayerAliveWinner")
public class LastPlayerAliveWinnerModule extends WorldModule {

    public LastPlayerAliveWinnerModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    void check() {
        List<BlurPlayer> players = getSession().getPlayers(Predicates.ALIVE);
        if (players.size() == 1) {
            PlayerWinner playerWinner = new PlayerWinner(players.iterator().next());
            getSession().callEvent(new GoalWinnersEvent(getSession(), Collections.singleton(playerWinner)));
            getStagesManager().nextStage(StageChangeReason.OBJECTIVE_SUCCESS);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isSession(event)) {
            check();
        }
    }
}
