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

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.Tick;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession.Predicates;
import com.blurengine.blur.session.Tickable;
import com.supaham.commons.bukkit.text.FancyMessage;

import org.bukkit.Location;

@ModuleInfo(name = "RandomCompassTarget")
public class RandomCompassTargetModule extends Module implements Tickable {

    private BlurPlayer bait;

    public RandomCompassTargetModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    @Tick(interval = "30s")
    private void updateBait() {
        getAnyPlayer(Predicates.ALIVE).ifPresent(p -> {
            this.bait = p;
            broadcastMessage(new FancyMessage().safeAppend("&e" + this.bait.getName() + " got baited!"));
        });
    }

    @Tick(interval = "1s")
    private void updateCompasses() {
        Location baitLoc = this.bait.getLocation();
        getPlayers(Predicates.ALIVE).forEach(aliveBP -> aliveBP.setCompassTarget(baitLoc));
    }
}
