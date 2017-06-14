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
import com.blurengine.blur.inventory.InventoryLayout;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.spawns.Spawn;
import com.supaham.commons.bukkit.players.BukkitPlayerManager;
import com.supaham.commons.bukkit.players.CommonPlayer;
import com.supaham.commons.bukkit.players.Players;
import com.supaham.commons.bukkit.text.FancyMessage;
import com.supaham.commons.bukkit.text.MessagePart;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;


/**
 * Represents a player that belongs to a {@link BlurSession}.
 */
public class BlurPlayer extends CommonPlayer implements Filter {

    private final BukkitPlayerManager manager;
    BlurSession blurSession;
    private boolean alive;
    private InventoryLayout inventoryLayout = new InventoryLayout(getPlayer().getInventory());
    private Map<Class, Object> customData = new HashMap<>();

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

        this.inventoryLayout = new InventoryLayout(player.getInventory());
        this.customData.clear();
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

    public BlurSession getSession() {
        return blurSession;
    }

    public boolean isAlive() {
        return alive;
    }

    protected void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Nonnull
    public InventoryLayout getInventoryLayout() {
        return inventoryLayout;
    }

    @Nonnull
    public Map<Class, Object> getCustomData() {
        return customData;
    }
    
    @Nullable
    public <T> T getCustomData(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        return (T) this.customData.get(clazz);
    }
    
    public void addCustomData(@Nonnull Object data) {
        Preconditions.checkNotNull(data, "data cannot be null.");
        this.customData.put(data.getClass(), data);
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
        setAlive(false);
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
        this.blurSession.callEvent(new PlayerKilledEvent(victim, victim.getLocation(), this));
    }

    /**
     * Respawns this player in the game, calling {@link BlurPlayerRespawnEvent} and setting {@link #isAlive()} to true.
     */
    public void respawn() {
        respawn(null);
    }

    /**
     * Respawns this player at a given spawn, calling {@link BlurPlayerRespawnEvent} and setting {@link #isAlive()} to true.
     * @param spawn where to respawn at
     */
        setAlive(true);
    public void respawn(Location location) {
        BlurPlayerRespawnEvent event = new BlurPlayerRespawnEvent(this, location);
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
    
    /* ================================
     * >> /DELEGATE METHODS
     * ================================ */

}
