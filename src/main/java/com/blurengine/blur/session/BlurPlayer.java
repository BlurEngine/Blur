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

package com.blurengine.blur.session;

import com.supaham.commons.bukkit.players.CommonPlayer;
import com.supaham.commons.bukkit.players.Players;
import com.supaham.commons.bukkit.text.FancyMessage;

import org.bukkit.entity.Player;

import lombok.NonNull;

/**
 * Represents a player that belongs to a {@link BlurSession}.
 */
public class BlurPlayer extends CommonPlayer implements BukkitPlayerDelegation {

    private final BlurSession session;
    private boolean alive;

    public BlurPlayer(@NonNull Player player, @NonNull BlurSession session) {
        super(player);
        this.session = session;
    }

    public void reset() {
        Player player = getPlayer();

        player.setItemOnCursor(null); // instead of close inventory to not close chat.
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setExhaustion(0);
        player.setSaturation(5);
        player.setFoodLevel(20);
        // Health
        player.resetMaxHealth();
        player.setHealth(player.getMaxHealth());
        // Flight/Walking speed, TODO disable flight?
        player.setWalkSpeed(Players.DEFAULT_WALK_SPEED);
        player.setFlySpeed(Players.DEFAULT_FLY_SPEED);
        // Experience
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        // Misc
        player.setRemainingAir(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.resetPlayerTime();
        player.resetPlayerWeather();
        player.setBedSpawnLocation(null);
    }

    public void messagePrefix(String string, Object... args) {
        message(new FancyMessage().append(session.getMessagePrefixMP()).append(String.format(string, args)));
    }

    public void messagePrefix(FancyMessage fancyMessage) {
        message(fancyMessage.add(0, session.getMessagePrefixMP()));
    }

    public BlurSession getSession() {
        return session;
    }

    public boolean isAlive() {
        return alive;
    }

    protected void setAlive(boolean alive) {
        this.alive = alive;
    }
}
