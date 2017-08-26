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

package com.blurengine.blur.modules.spawns;

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.players.BlurPlayerRespawnEvent;
import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.framework.WorldModule;
import com.blurengine.blur.modules.extents.BlockExtent;
import com.blurengine.blur.modules.extents.ExtentNotFoundException;
import com.blurengine.blur.modules.spawns.SpawnsModule.SpawnsData;
import com.blurengine.blur.modules.spawns.serializer.SpawnSerializer;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.utils.CollectionUtils;
import com.supaham.commons.utils.StringUtils;
import com.supaham.commons.utils.WeakSet;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.SerializeWith;

@ModuleInfo(name = "Spawns", dataClass = SpawnsData.class)
public class SpawnsModule extends WorldModule {

    private final SpawnsData data;
    /** Contains a list of players respawning forcefully by this module. See spawnPlayer method with spigot respawn call.*/
    private final WeakSet<Player> validRespawningPlayers = new WeakSet<>();

    public static Location getLocationFromSpawn(Spawn spawn, World world, Entity entity) {
        Preconditions.checkNotNull(spawn, "spawn cannot be null.");
        Preconditions.checkNotNull(world, "world cannot be null.");
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Location location = spawn.getExtent().getRandomLocation().toLocation(world);
        spawn.getSpawnDirection().applyTo(location, entity);
        return location;
    }

    public SpawnsModule(ModuleManager moduleManager, SpawnsData data) {
        super(moduleManager);
        this.data = data;
    }

    @EventHandler
    public void onSessionStart(SessionStartEvent event) {
        if (isSession(event)) {

            newTask(() -> getPlayers().forEach(p -> {
                Spawn spawn = data.spawnOnStart;
                if (spawn == null) {
                    spawn = getNextSpawnForEntity(p.getPlayer());
                }
                p.respawn(getLocationFromSpawn(spawn, getWorld(), p.getPlayer()));
            })).delay(0).build();
        }
    }

    @EventHandler
    public void onPlayerJoinSession(PlayerJoinSessionEvent event) {
        if (isSession(event)) {
            if (event.getSession().isStarted()) {
                event.getBlurPlayer().respawn();
// FIXME This code is commented out as it causes the players to spawn when the session hasn't started. This isn't ideal as it causes two spawns to occur
// when the session is created via LobbyModule or the likes.
//            } else {
//                spawnPlayer(event.getBlurPlayer(), data.spawnOnStart == null ? data.defaultSpawn : data.spawnOnStart);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void setInitialNextSpawnOnPlayerRespawn(BlurPlayerRespawnEvent event) {
        if (isSession(event)) {
            if (event.getSpawnLocation() == null) {
                Player player = event.getBlurPlayer().getPlayer();
                Spawn spawn = getNextSpawnForEntity(player);
                Location spawnLocation = getLocationFromSpawn(spawn, getWorld(), player);
                event.setSpawnLocation(spawnLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlurPlayerRespawn(BlurPlayerRespawnEvent event) {
        if (isSession(event)) {
            if (event.getSpawnLocation() != null) {
                spawnPlayer(event.getBlurPlayer(), event.getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!this.validRespawningPlayers.contains(event.getPlayer())) {
            return;
        }
        BlurPlayer blurPlayer = getSession().getPlayer(event.getPlayer());
        if (isSession(blurPlayer.getSession())) {
            Location location = getLocationFromSpawn(getNextSpawnForEntity(event.getPlayer()), getWorld(), event.getPlayer());
            getLogger().finer("Spawning %s at %s", blurPlayer.getName(), location);
            event.setRespawnLocation(location);
        }
    }

    public void spawnPlayer(@Nonnull BlurPlayer blurPlayer) {
        spawnPlayer(blurPlayer, getNextSpawnForEntity(blurPlayer.getPlayer()));
    }

    public void spawnPlayer(@Nonnull BlurPlayer blurPlayer, @Nonnull Spawn spawn) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        Preconditions.checkNotNull(spawn, "spawn cannot be null.");
        if (blurPlayer.getPlayer().isDead()) {
            this.validRespawningPlayers.add(blurPlayer.getPlayer());
            blurPlayer.getPlayer().spigot().respawn();
            this.validRespawningPlayers.remove(blurPlayer.getPlayer());
        }

        Location location = getLocationFromSpawn(spawn, getWorld(), blurPlayer.getPlayer());
        spawnPlayer(blurPlayer, location);
    }

    public void spawnPlayer(@Nonnull BlurPlayer blurPlayer, @Nonnull Location location) {
        getLogger().finer("Spawning %s at %s", blurPlayer.getName(), location);
        blurPlayer.getPlayer().teleport(location);
    }

    public Spawn getAnySpawn() {
        if (data.spawns.isEmpty()) {
            return data.defaultSpawn;
        }
        return CollectionUtils.getRandomElement(data.spawns);
    }

    public Spawn getNextSpawnForEntity(Entity entity) {
        return data.spawns.stream().filter(s -> s.getFilter().test(entity).isAllowed()).findFirst()
            .orElse(data.defaultSpawn);
    }

    public static final class SpawnsData implements ModuleData {

        public static final String DEFAULT_SPAWN = "default-spawn";

        @Name("default")
        private Spawn defaultSpawn;
        @SerializeWith(SpawnSerializer.class)
        private List<Spawn> spawns;
        @Name("spawn-on-start")
        private Spawn spawnOnStart;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof Map) {
                Map<String, Object> asMap = serialized.getAsMap();

                // The following code is a layer of convenience for the user input
                // if input is null or true set to DEFAULT_SPAWN, if 'false' set to null, meaning don't spawn players on start.
                Function<Boolean, String> bFunction = b -> b ? DEFAULT_SPAWN : null;
                if (asMap.containsKey("spawn-on-start")) {
                    Object obj = asMap.get("spawn-on-start");
                    String spawnOnStartValue;
                    if (obj == null) {
                        spawnOnStartValue = bFunction.apply(true); // emulate default setting
                    } else if (obj instanceof Boolean) {
                        spawnOnStartValue = bFunction.apply(((Boolean) obj));
                    } else {
                        String str = obj.toString().trim();
                        spawnOnStartValue = str.isEmpty() ? DEFAULT_SPAWN : StringUtils.parseBoolean(str).map(bFunction).orElse(str);
                    }
                    if (spawnOnStartValue == null) { // Explicit null, don't deserialize spawn-on-start.
                        asMap.remove("spawn-on-start");
                    } else {
                        asMap.put("spawn-on-start", spawnOnStartValue);
                    }
                }

                serialized.load(this);
            }


            /*
             * If no default spawn is specified, we must set one internally by default based on BlockExtent.ZERO. This ensures there is always a
             * fallback to spawn players to. Despite how chaotic it may seem, it is up to the user to fix the issue of spawns. 
             */
            if (this.defaultSpawn == null) {
                try { // Use extent by id DEFAULT_SPAWN
                    this.defaultSpawn = moduleManager.getModuleLoader().getSpawnSerializer()
                        .deserialize(DEFAULT_SPAWN, Spawn.class, serialized.getSerializerSet());
                } catch (ExtentNotFoundException e) { // force set fallback spawn
                    this.defaultSpawn = new Spawn(BlockExtent.ZERO);
                }
            }

            if (this.spawns == null) {
                this.spawns = new ArrayList<>(1);
            }
            return new SpawnsModule(moduleManager, this);
        }
    }
}
