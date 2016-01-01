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

import com.supaham.commons.bukkit.text.FancyMessage;
import com.supaham.commons.bukkit.text.MessagePart;
import com.supaham.commons.bukkit.title.Title;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

/**
 * Created by Ali on 22/11/2015.
 */
@SuppressWarnings("unused")
public interface BukkitPlayerDelegation {

    Player getPlayer();
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    default InetSocketAddress getRawAddress() {
        return getPlayer().spigot().getRawAddress();
    }

    default void playEffect(Location location, Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed,
                            int particleCount, int radius) {
        getPlayer().spigot().playEffect(location, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, radius);
    }

    default boolean getCollidesWithEntities() {
        return getPlayer().spigot().getCollidesWithEntities();
    }

    default void setCollidesWithEntities(boolean collides) {
        getPlayer().spigot().setCollidesWithEntities(collides);
    }

    default void respawn() {
        getPlayer().spigot().respawn();
    }

    default String getLocale() {
        return getPlayer().spigot().getLocale();
    }

    default Set<Player> getHiddenPlayers() {
        return getPlayer().spigot().getHiddenPlayers();
    }

    default String getDisplayName() {
        return getPlayer().getDisplayName();
    }

    default void setDisplayName(String name) {
        getPlayer().setDisplayName(name);
    }

    default String getPlayerListName() {
        return getPlayer().getPlayerListName();
    }

    default void setPlayerListName(String name) {
        getPlayer().setPlayerListName(name);
    }

    default void setCompassTarget(Location loc) {
        getPlayer().setCompassTarget(loc);
    }

    default Location getCompassTarget() {
        return getPlayer().getCompassTarget();
    }

    default InetSocketAddress getAddress() {
        return getPlayer().getAddress();
    }

    default boolean isSneaking() {
        return getPlayer().isSneaking();
    }

    default void setSneaking(boolean sneak) {
        getPlayer().setSneaking(sneak);
    }

    default boolean isSprinting() {
        return getPlayer().isSprinting();
    }

    default void setSprinting(boolean sprinting) {
        getPlayer().setSprinting(sprinting);
    }

    default void setSleepingIgnored(boolean isSleeping) {
        getPlayer().setSleepingIgnored(isSleeping);
    }

    default boolean isSleepingIgnored() {
        return getPlayer().isSleepingIgnored();
    }

    default void sendBlockChange(Location loc, Material material, byte data) {
        getPlayer().sendBlockChange(loc, material, data);
    }

    default boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
        return getPlayer().sendChunkChange(loc, sx, sy, sz, data);
    }

    default void sendBlockChange(Location loc, int material, byte data) {
        getPlayer().sendBlockChange(loc, material, data);
    }

    default void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
        getPlayer().sendSignChange(loc, lines);
    }

    default void updateInventory() {
        getPlayer().updateInventory();
    }

    default void awardAchievement(Achievement achievement) {
        getPlayer().awardAchievement(achievement);
    }

    default void removeAchievement(Achievement achievement) {
        getPlayer().removeAchievement(achievement);
    }

    default boolean hasAchievement(Achievement achievement) {
        return getPlayer().hasAchievement(achievement);
    }

    default void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic);
    }

    default void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
        getPlayer().decrementStatistic(statistic);
    }

    default void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic, amount);
    }

    default void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        getPlayer().decrementStatistic(statistic, amount);
    }

    default void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
        getPlayer().setStatistic(statistic, newValue);
    }

    default int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return getPlayer().getStatistic(statistic);
    }

    default void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic, material);
    }

    default void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        getPlayer().decrementStatistic(statistic, material);
    }

    default int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return getPlayer().getStatistic(statistic, material);
    }

    default void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic, material, amount);
    }

    default void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        getPlayer().decrementStatistic(statistic, material, amount);
    }

    default void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
        getPlayer().setStatistic(statistic, material, newValue);
    }

    default void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic, entityType);
    }

    default void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        getPlayer().decrementStatistic(statistic, entityType);
    }

    default int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return getPlayer().getStatistic(statistic, entityType);
    }

    default void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
        throws IllegalArgumentException {
        getPlayer().incrementStatistic(statistic, entityType, amount);
    }

    default void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        getPlayer().decrementStatistic(statistic, entityType, amount);
    }

    default void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        getPlayer().setStatistic(statistic, entityType, newValue);
    }

    default void setPlayerTime(long time, boolean relative) {
        getPlayer().setPlayerTime(time, relative);
    }

    default long getPlayerTime() {
        return getPlayer().getPlayerTime();
    }

    default long getPlayerTimeOffset() {
        return getPlayer().getPlayerTimeOffset();
    }

    default boolean isPlayerTimeRelative() {
        return getPlayer().isPlayerTimeRelative();
    }

    default void resetPlayerTime() {
        getPlayer().resetPlayerTime();
    }

    default void setPlayerWeather(WeatherType type) {
        getPlayer().setPlayerWeather(type);
    }

    default WeatherType getPlayerWeather() {
        return getPlayer().getPlayerWeather();
    }

    default void resetPlayerWeather() {
        getPlayer().resetPlayerWeather();
    }

    default void giveExp(int amount) {
        getPlayer().giveExp(amount);
    }

    default void giveExpLevels(int amount) {
        getPlayer().giveExpLevels(amount);
    }

    default float getExp() {
        return getPlayer().getExp();
    }

    default void setExp(float exp) {
        getPlayer().setExp(exp);
    }

    default int getLevel() {
        return getPlayer().getLevel();
    }

    default void setLevel(int level) {
        getPlayer().setLevel(level);
    }

    default int getTotalExperience() {
        return getPlayer().getTotalExperience();
    }

    default void setTotalExperience(int exp) {
        getPlayer().setTotalExperience(exp);
    }

    default float getExhaustion() {
        return getPlayer().getExhaustion();
    }

    default void setExhaustion(float value) {
        getPlayer().setExhaustion(value);
    }

    default float getSaturation() {
        return getPlayer().getSaturation();
    }

    default void setSaturation(float value) {
        getPlayer().setSaturation(value);
    }

    default int getFoodLevel() {
        return getPlayer().getFoodLevel();
    }

    default void setFoodLevel(int value) {
        getPlayer().setFoodLevel(value);
    }

    default Location getBedSpawnLocation() {
        return getPlayer().getBedSpawnLocation();
    }

    default void setBedSpawnLocation(Location location) {
        getPlayer().setBedSpawnLocation(location);
    }

    default void setBedSpawnLocation(Location location, boolean force) {
        getPlayer().setBedSpawnLocation(location, force);
    }

    default boolean getAllowFlight() {
        return getPlayer().getAllowFlight();
    }

    default void setAllowFlight(boolean flight) {
        getPlayer().setAllowFlight(flight);
    }

    default void hidePlayer(Player player) {
        getPlayer().hidePlayer(player);
    }

    default void showPlayer(Player player) {
        getPlayer().showPlayer(player);
    }

    default boolean canSee(Player player) {
        return getPlayer().canSee(player);
    }

    default boolean isOnGround() {
        return getPlayer().isOnGround();
    }

    default boolean isFlying() {
        return getPlayer().isFlying();
    }

    default void setFlying(boolean value) {
        getPlayer().setFlying(value);
    }

    default void setFlySpeed(float value) throws IllegalArgumentException {
        getPlayer().setFlySpeed(value);
    }

    default void setWalkSpeed(float value) throws IllegalArgumentException {
        getPlayer().setWalkSpeed(value);
    }

    default float getFlySpeed() {
        return getPlayer().getFlySpeed();
    }

    default float getWalkSpeed() {
        return getPlayer().getWalkSpeed();
    }

    default void setResourcePack(String url) {
        getPlayer().setResourcePack(url);
    }

    default Scoreboard getScoreboard() {
        return getPlayer().getScoreboard();
    }

    default void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        getPlayer().setScoreboard(scoreboard);
    }

    default boolean isHealthScaled() {
        return getPlayer().isHealthScaled();
    }

    default void setHealthScaled(boolean scale) {
        getPlayer().setHealthScaled(scale);
    }

    default void setHealthScale(double scale) throws IllegalArgumentException {
        getPlayer().setHealthScale(scale);
    }

    default double getHealthScale() {
        return getPlayer().getHealthScale();
    }

    default Entity getSpectatorTarget() {
        return getPlayer().getSpectatorTarget();
    }

    default void setSpectatorTarget(Entity entity) {
        getPlayer().setSpectatorTarget(entity);
    }

    default String getName() {
        return getPlayer().getName();
    }

    default PlayerInventory getInventory() {
        return getPlayer().getInventory();
    }

    default Inventory getEnderChest() {
        return getPlayer().getEnderChest();
    }

    default boolean setWindowProperty(Property prop, int value) {
        return getPlayer().setWindowProperty(prop, value);
    }

    default InventoryView getOpenInventory() {
        return getPlayer().getOpenInventory();
    }

    default InventoryView openInventory(Inventory inventory) {
        return getPlayer().openInventory(inventory);
    }

    default InventoryView openWorkbench(Location location, boolean force) {
        return getPlayer().openWorkbench(location, force);
    }

    default InventoryView openEnchanting(Location location, boolean force) {
        return getPlayer().openEnchanting(location, force);
    }

    default void openInventory(InventoryView inventory) {
        getPlayer().openInventory(inventory);
    }

    default void closeInventory() {
        getPlayer().closeInventory();
    }

    default ItemStack getItemInHand() {
        return getPlayer().getItemInHand();
    }

    default void setItemInHand(ItemStack item) {
        getPlayer().setItemInHand(item);
    }

    default ItemStack getItemOnCursor() {
        return getPlayer().getItemOnCursor();
    }

    default void setItemOnCursor(ItemStack item) {
        getPlayer().setItemOnCursor(item);
    }

    default boolean isSleeping() {
        return getPlayer().isSleeping();
    }

    default int getSleepTicks() {
        return getPlayer().getSleepTicks();
    }

    default GameMode getGameMode() {
        return getPlayer().getGameMode();
    }

    default void setGameMode(GameMode mode) {
        getPlayer().setGameMode(mode);
    }

    default boolean isBlocking() {
        return getPlayer().isBlocking();
    }

    default int getExpToLevel() {
        return getPlayer().getExpToLevel();
    }

    default double getEyeHeight() {
        return getPlayer().getEyeHeight();
    }

    default double getEyeHeight(boolean ignoreSneaking) {
        return getPlayer().getEyeHeight(ignoreSneaking);
    }

    default Location getEyeLocation() {
        return getPlayer().getEyeLocation();
    }

    default List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return getPlayer().getLineOfSight(transparent, maxDistance);
    }

    default Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return getPlayer().getTargetBlock(transparent, maxDistance);
    }

    default int getRemainingAir() {
        return getPlayer().getRemainingAir();
    }

    default void setRemainingAir(int ticks) {
        getPlayer().setRemainingAir(ticks);
    }

    default int getMaximumAir() {
        return getPlayer().getMaximumAir();
    }

    default void setMaximumAir(int ticks) {
        getPlayer().setMaximumAir(ticks);
    }

    default int getMaximumNoDamageTicks() {
        return getPlayer().getMaximumNoDamageTicks();
    }

    default void setMaximumNoDamageTicks(int ticks) {
        getPlayer().setMaximumNoDamageTicks(ticks);
    }

    default double getLastDamage() {
        return getPlayer().getLastDamage();
    }

    default void setLastDamage(double damage) {
        getPlayer().setLastDamage(damage);
    }

    default int getNoDamageTicks() {
        return getPlayer().getNoDamageTicks();
    }

    default void setNoDamageTicks(int ticks) {
        getPlayer().setNoDamageTicks(ticks);
    }

    default Player getKiller() {
        return getPlayer().getKiller();
    }

    default boolean addPotionEffect(PotionEffect effect) {
        return getPlayer().addPotionEffect(effect);
    }

    default boolean addPotionEffect(PotionEffect effect, boolean force) {
        return getPlayer().addPotionEffect(effect, force);
    }

    default boolean addPotionEffects(Collection<PotionEffect> effects) {
        return getPlayer().addPotionEffects(effects);
    }

    default boolean hasPotionEffect(PotionEffectType type) {
        return getPlayer().hasPotionEffect(type);
    }

    default void removePotionEffect(PotionEffectType type) {
        getPlayer().removePotionEffect(type);
    }

    default Collection<PotionEffect> getActivePotionEffects() {
        return getPlayer().getActivePotionEffects();
    }

    default boolean hasLineOfSight(Entity other) {
        return getPlayer().hasLineOfSight(other);
    }

    default boolean getRemoveWhenFarAway() {
        return getPlayer().getRemoveWhenFarAway();
    }

    default void setRemoveWhenFarAway(boolean remove) {
        getPlayer().setRemoveWhenFarAway(remove);
    }

    default EntityEquipment getEquipment() {
        return getPlayer().getEquipment();
    }

    default void setCanPickupItems(boolean pickup) {
        getPlayer().setCanPickupItems(pickup);
    }

    default boolean getCanPickupItems() {
        return getPlayer().getCanPickupItems();
    }

    default boolean isLeashed() {
        return getPlayer().isLeashed();
    }

    default Entity getLeashHolder() throws IllegalStateException {
        return getPlayer().getLeashHolder();
    }

    default boolean setLeashHolder(Entity holder) {
        return getPlayer().setLeashHolder(holder);
    }

    default void damage(double amount) {
        getPlayer().damage(amount);
    }

    default void damage(double amount, Entity source) {
        getPlayer().damage(amount, source);
    }

    default double getHealth() {
        return getPlayer().getHealth();
    }

    default void setHealth(double health) {
        getPlayer().setHealth(health);
    }

    default double getMaxHealth() {
        return getPlayer().getMaxHealth();
    }

    default void setMaxHealth(double health) {
        getPlayer().setMaxHealth(health);
    }

    default void resetMaxHealth() {
        getPlayer().resetMaxHealth();
    }

    default Location getLocation() {
        return getPlayer().getLocation();
    }

    default Location getLocation(Location loc) {
        return getPlayer().getLocation(loc);
    }

    default void setVelocity(Vector velocity) {
        getPlayer().setVelocity(velocity);
    }

    default Vector getVelocity() {
        return getPlayer().getVelocity();
    }

    default World getWorld() {
        return getPlayer().getWorld();
    }

    default boolean teleport(Location location) {
        return getPlayer().teleport(location);
    }

    default boolean teleport(Location location, TeleportCause cause) {
        return getPlayer().teleport(location, cause);
    }

    default boolean teleport(Entity destination) {
        return getPlayer().teleport(destination);
    }

    default boolean teleport(Entity destination, TeleportCause cause) {
        return getPlayer().teleport(destination, cause);
    }

    default List<Entity> getNearbyEntities(double x, double y, double z) {
        return getPlayer().getNearbyEntities(x, y, z);
    }

    default int getEntityId() {
        return getPlayer().getEntityId();
    }

    default int getFireTicks() {
        return getPlayer().getFireTicks();
    }

    default int getMaxFireTicks() {
        return getPlayer().getMaxFireTicks();
    }

    default void setFireTicks(int ticks) {
        getPlayer().setFireTicks(ticks);
    }

    default boolean isDead() {
        return getPlayer().isDead();
    }

    default boolean isValid() {
        return getPlayer().isValid();
    }

    default Entity getPassenger() {
        return getPlayer().getPassenger();
    }

    default boolean setPassenger(Entity passenger) {
        return getPlayer().setPassenger(passenger);
    }

    default boolean isEmpty() {
        return getPlayer().isEmpty();
    }

    default boolean eject() {
        return getPlayer().eject();
    }

    default float getFallDistance() {
        return getPlayer().getFallDistance();
    }

    default void setFallDistance(float distance) {
        getPlayer().setFallDistance(distance);
    }

    default void setLastDamageCause(EntityDamageEvent event) {
        getPlayer().setLastDamageCause(event);
    }

    default EntityDamageEvent getLastDamageCause() {
        return getPlayer().getLastDamageCause();
    }

    default int getTicksLived() {
        return getPlayer().getTicksLived();
    }

    default void setTicksLived(int value) {
        getPlayer().setTicksLived(value);
    }

    default void playEffect(EntityEffect type) {
        getPlayer().playEffect(type);
    }

    default EntityType getType() {
        return getPlayer().getType();
    }

    default boolean isInsideVehicle() {
        return getPlayer().isInsideVehicle();
    }

    default boolean leaveVehicle() {
        return getPlayer().leaveVehicle();
    }

    default Entity getVehicle() {
        return getPlayer().getVehicle();
    }

    default void sendMessage(String message) {
        getPlayer().sendMessage(message);
    }

    default void sendMessage(String[] messages) {
        getPlayer().sendMessage(messages);
    }

    default boolean isPermissionSet(String name) {
        return getPlayer().isPermissionSet(name);
    }

    default boolean isPermissionSet(Permission perm) {
        return getPlayer().isPermissionSet(perm);
    }

    default boolean hasPermission(String name) {
        return getPlayer().hasPermission(name);
    }

    default boolean hasPermission(Permission perm) {
        return getPlayer().hasPermission(perm);
    }

    default <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return getPlayer().launchProjectile(projectile);
    }

    default <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        return getPlayer().launchProjectile(projectile, velocity);
    }
}
