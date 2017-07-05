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

package com.blurengine.blur.modules;

import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ticking.Tick;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession.Predicates;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import org.bukkit.Location;

@ModuleInfo(name = "RandomCompassTarget")
public class RandomCompassTargetModule extends Module {

    private BlurPlayer bait;

    public RandomCompassTargetModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    @Tick(interval = 30000, ms = true)
    private void updateBait() {
        getRandomPlayer(Predicates.ALIVE).ifPresent(p -> {
            this.bait = p;
            broadcastMessage(TextComponent.of(this.bait.getName() + " got baited!").color(TextColor.YELLOW));
        });
    }

    @Tick(interval = 1000, ms = true)
    private void updateCompasses() {
        Location baitLoc = this.bait.getPlayer().getLocation();
        getPlayers(Predicates.ALIVE).forEach(aliveBP -> aliveBP.getPlayer().setCompassTarget(baitLoc));
    }
}
