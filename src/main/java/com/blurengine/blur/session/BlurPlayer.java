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

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.players.BlurPlayerDeathEvent;
import com.blurengine.blur.events.players.BlurPlayerRespawnEvent;
import com.blurengine.blur.events.players.PlayerDamagePlayerEvent;
import com.blurengine.blur.events.players.PlayerKilledEvent;
import com.blurengine.blur.framework.metadata.MetadataHolder;
import com.blurengine.blur.framework.playerdata.PlayerData;
import com.blurengine.blur.inventory.InventoryLayout;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.message.Message;
import com.supaham.commons.bukkit.players.BukkitPlayerManager;
import com.supaham.commons.bukkit.players.CommonPlayer;
import com.supaham.commons.bukkit.players.Players;
import com.supaham.commons.bukkit.text.FancyMessage;
import com.supaham.commons.bukkit.text.MessagePart;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Represents a player that belongs to a {@link BlurSession}.
 */
public class BlurPlayer extends CommonPlayer implements Filter, MetadataHolder {

    private final BukkitPlayerManager manager;
    BlurSession blurSession;

    public BlurPlayer(BukkitPlayerManager manager, @Nonnull Player player) {
        super(player);
        this.manager = Preconditions.checkNotNull(manager, "manager cannot be null.");
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

        BlurPlayerCoreData coreData = getCoreData();
        coreData.inventoryLayout = new InventoryLayout(player.getInventory());
    }

    public void messagePrefix(String string, Object... args) {
        messagePrefix(new FancyMessage(String.format(string, args)));
    }

    public void messagePrefix(FancyMessage fancyMessage) {
        List<MessagePart> parts = blurSession.getMessagePrefix().getMessageParts();
        // Add prefix like so to make immutable. Extreme flaw in design of FancyMessage
        for (int i = 0; i < parts.size(); i++) {
            fancyMessage.add(i, parts.get(i));
        }
        message(fancyMessage);
    }

    public boolean messageTl(String messageNode, Object... args) {
        return getSession().getModuleManager().getMessagesManager().sendMessage(this, messageNode, args);
    }

    public BlurSession getSession() {
        return blurSession;
    }

    @Nonnull
    public BlurPlayerCoreData getCoreData() {
        return getMetadata(BlurPlayerCoreData.class);
    }

    @Override
    public FilterResponse test(Object object) {
        if (object instanceof Player) {
            return FilterResponse.from(getPlayer() == object);
        } else if (object instanceof UUID) {
            return FilterResponse.from(getUuid().equals(object));
        } else if (object instanceof String) {
            return FilterResponse.from(getName().equals(object));
        }
        return FilterResponse.ABSTAIN;
    }

    /**
     * Kills this player by calling {@link BlurPlayerDeathEvent}.
     */
    public void die() {
        BlurPlayerCoreData coreData = getCoreData();
        coreData.setDeaths(coreData.getDeaths() + 1);
        coreData.setAlive(false);
        this.blurSession.callEvent(new BlurPlayerDeathEvent(this, getLocation()));
    }

    /**
     * Kills a {@link BlurPlayer} using details provided in the {@link PlayerDamagePlayerEvent} parameter.
     * @param event Blur PlayerDamagePlayerEvent 
     */
    public void kill(@Nonnull PlayerDamagePlayerEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null.");
        BlurPlayer victim = event.getVictim();
        victim.die();
        BlurPlayerCoreData coreData = getCoreData();
        coreData.setKills(coreData.getKills() + 1);
        this.blurSession.callEvent(new PlayerKilledEvent(victim, victim.getLocation(), this));
    }

    /**
     * Respawns this player in the game, calling {@link BlurPlayerRespawnEvent} and setting {@link BlurPlayerCoreData#isAlive()} to true.
     */
    public void respawn() {
        respawn(null);
    }

    /**
     * Respawns this player at a given spawn, calling {@link BlurPlayerRespawnEvent} and setting {@link BlurPlayerCoreData#isAlive()} to true.
     * @param location where to respawn at
     */
    public void respawn(Location location) {
        BlurPlayerRespawnEvent event = new BlurPlayerRespawnEvent(this, location);
        getCoreData().setAlive(true);
        getSession().callEvent(event);
    }

    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public String getDisplayName() {
        return getPlayer().getDisplayName();
    }

    public Location getLocation() {
        return getPlayer().getLocation();
    }

    public Location getEyeLocation() {
        return getPlayer().getEyeLocation();
    }

    @Override
    public boolean hasMetadata(Object object) {
        return getSession().getPlayerMetadata().contains(this, object);
    }

    @Override
    public <T> boolean hasMetadata(Class<T> metadataClass) {
        return getSession().getPlayerMetadata().contains(this, metadataClass);
    }

    @Override
    public <T> T getMetadata(Class<T> metadataClass) {
        return getSession().getPlayerMetadata().get(this, metadataClass);
    }

    @Override
    public Object putMetadata(Object object) {
        return getSession().getPlayerMetadata().put(this, object);
    }

    /* ================================
     * >> /DELEGATE METHODS
     * ================================ */

    public static final class BlurPlayerCoreData implements PlayerData {

        private final BlurPlayer blurPlayer;
        private boolean alive;
        private InventoryLayout inventoryLayout;
        private int kills;
        private int deaths;

        public BlurPlayerCoreData(@Nonnull BlurPlayer blurPlayer) {
            Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
            this.blurPlayer = blurPlayer;
            this.inventoryLayout = new InventoryLayout(blurPlayer.getPlayer().getInventory());
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
    }
}
