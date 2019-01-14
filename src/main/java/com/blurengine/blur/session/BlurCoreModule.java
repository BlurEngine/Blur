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

package com.blurengine.blur.session;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.InternalModule;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.metadata.playerdata.PlayerData;
import com.blurengine.blur.inventory.InventoryLayout;

import java.time.Instant;

import javax.annotation.Nonnull;

@ModuleInfo(name = "BlurCore")
@InternalModule
public class BlurCoreModule extends Module {

    public BlurCoreModule(ModuleManager moduleManager) {
        super(moduleManager);
        getPlayerMetadataCreator().registerClass(BlurPlayerCoreData.class);
    }

    public static final class BlurPlayerCoreData implements PlayerData {

        private final BlurPlayer blurPlayer;
        private boolean alive;
        private InventoryLayout inventoryLayout;
        private int kills;
        private int deaths;
        private final Instant sessionJoinTime;

        public BlurPlayerCoreData(@Nonnull BlurPlayer blurPlayer) {
            Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
            this.blurPlayer = blurPlayer;
            this.inventoryLayout = new InventoryLayout(blurPlayer.getPlayer().getInventory());

            // Assumes this class is created as soon as the player joins the session. Can be placed better.
            sessionJoinTime = Instant.now();
        }

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        public InventoryLayout getInventoryLayout() {
            return inventoryLayout;
        }

        public void setInventoryLayout(InventoryLayout inventoryLayout) {
            this.inventoryLayout = inventoryLayout;
        }

        public int getKills() {
            return kills;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public void setDeaths(int deaths) {
            this.deaths = deaths;
        }

        public Instant getSessionJoinTime() {
            return sessionJoinTime;
        }
    }
}
