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
import com.blurengine.blur.inventory.InventoryLayout;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.session.BlurCoreModule.BlurPlayerCoreData;
import com.supaham.commons.bukkit.players.BukkitPlayerManager;
import com.supaham.commons.bukkit.players.CommonPlayer;
import com.supaham.commons.bukkit.players.Players;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
        coreData.setInventoryLayout(new InventoryLayout(player.getInventory()));
    }

    public void messagePrefix(String string, Object... args) {
        messagePrefix(TextComponent.of(String.format(string, args)));
    }

    public void messagePrefix(Component component) {
        message(blurSession.getMessagePrefix().append(component));
    }

    public boolean messageTl(String messageNode, Object... args) {
        return getSession().getModuleManager().getMessagesManager().sendMessage(this, messageNode, args, null);
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
        kill(event.getVictim());
    }

    /**
     * Kills a {@link BlurPlayer}.
     * @param victim Victim 
     */
    public void kill(@Nonnull BlurPlayer victim) {
        Preconditions.checkNotNull(victim, "victim");
        BlurPlayerCoreData coreData = getCoreData();
        coreData.setKills(coreData.getKills() + 1);
        this.blurSession.callEvent(new PlayerKilledEvent(victim, victim.getLocation(), this));
        victim.die();
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

    @Nonnull
    @Override
    public List<Object> removeAll() {
        return getSession().getPlayerMetadata().removeAll(this);
    }

    @Override
    public <T> boolean removeMetadata(T object) {
        return getSession().getPlayerMetadata().remove(this, object);
    }

    @Nullable
    @Override
    public <T> T removeMetadata(Class<T> metadataClass) {
        return getSession().getPlayerMetadata().remove(this, metadataClass);
    }

    /* ================================
     * >> /DELEGATE METHODS
     * ================================ */

}
