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

import com.blurengine.blur.BlurPlugin;
import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.framework.WorldModule;
import com.blurengine.blur.modules.spawns.SpawnsModule.SpawnsData;
import com.blurengine.blur.serializers.SpawnList;
import com.blurengine.blur.session.BlurPlayer;
import com.sk89q.intake.Command;
import com.supaham.commons.utils.CollectionUtils;
import com.supaham.commons.utils.StringUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import javaslang.control.Match;
import pluginbase.config.annotation.Name;

@ModuleInfo(name = "Spawns", dataClass = SpawnsData.class)
public class SpawnsModule extends WorldModule {

    private static Commands COMMANDS;
    private final SpawnsData data;

    public SpawnsModule(ModuleManager moduleManager, SpawnsData data) {
        super(moduleManager);
        this.data = data;
        if (getLogger().getDebugLevel() >= 2 && COMMANDS == null) {
            COMMANDS = new Commands();
            getSession().getBlur().getPlugin().getCommandsManager().builder().registerMethods(COMMANDS);
            getSession().getBlur().getPlugin().getCommandsManager().build();
        }
    }

    @EventHandler
    public void onSessionStart(SessionStartEvent event) {
        if (isSession(event)) {
            if (data.spawnOnStart != null) {
                newTask(() -> getPlayers().forEach(p -> spawnPlayer(p, data.spawnOnStart))).delay(0).build();
            }
        }
    }

    @EventHandler
    public void onPlayerJoinSession(PlayerJoinSessionEvent event) {
        if (isSession(event)) {
            if (event.getSession().isStarted()) {
                spawnPlayer(event.getBlurPlayer(), getNextSpawn());
// FIXME This code is commented out as it causes the players to spawn when the session hasn't started. This isn't ideal as it causes two spawns to occur
// when the session is created via LobbyModule or the likes.
//            } else {
//                spawnPlayer(event.getBlurPlayer(), data.spawnOnStart == null ? data.defaultSpawn : data.spawnOnStart);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        BlurPlayer blurPlayer = getSession().getPlayer(event.getPlayer());
        if (isSession(blurPlayer.getSession())) {
            event.setRespawnLocation(getNextSpawnLocationFor(event.getPlayer()));
        }
    }

    public void spawnPlayer(@Nonnull BlurPlayer blurPlayer) {
        spawnPlayer(blurPlayer, getNextSpawn());
    }

    public void spawnPlayer(@Nonnull BlurPlayer blurPlayer, @Nonnull Spawn spawn) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        Preconditions.checkNotNull(spawn, "spawn cannot be null.");
        if (blurPlayer.getPlayer().isDead()) {
            blurPlayer.getPlayer().spigot().respawn();
        }

        Location location = getNextSpawnLocationFor(blurPlayer.getPlayer());
        getLogger().finer("Spawning %s at %s", blurPlayer.getName(), location);
        blurPlayer.teleport(location);
    }

    public Spawn getNextSpawn() {
        return CollectionUtils.getRandomElement(data.spawns);
    }

    public Location getNextSpawnLocationFor(Entity entity) {
        Spawn spawn = getNextSpawn();
        Location location = spawn.getExtent().getRandomLocation().toLocation(getSession().getWorld());
        spawn.getSpawnDirection().applyTo(location, entity);
        return location;
    }

    public static final class SpawnsData implements ModuleData {

        @Name("default")
        private Spawn defaultSpawn;
        private SpawnList spawns;
        @Name("spawn-on-start")
        private Spawn spawnOnStart;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof Map) {
                Map<String, Object> asMap = serialized.getAsMap();

                // The following code is a layer of convenience for the user input
                // if input is null or true set to 'default', if 'false' set to null, meaning don't spawn players on start.
                Function<Boolean, String> bFunction = b -> b ? "default" : null;
                String aDefault = Match.of(asMap.get("spawn-on-start"))
                    .whenType(Boolean.class).then(bFunction::apply)
                    .whenIs(null).then(bFunction.apply(true))
                    .otherwise(o -> {
                        String str = o.toString().trim();
                        return str.isEmpty() ? "default" : StringUtils.parseBoolean(str).map(bFunction).orElse(str);
                    })
                    .getOrElse((String) null); // Allow null
                if (aDefault == null) { // Explicit null, don't deserialize spawn-on-start.
                    asMap.remove("spawn-on-start");
                } else {
                    asMap.put("spawn-on-start", aDefault);
                }

                if (!asMap.containsKey("default")) {
                    asMap.put("default", "default");
                }

                serialized.load(this);
            } else {
                defaultSpawn = moduleManager.getModuleLoader().getSpawnSerializer().deserialize("default", null, null);
            }

            checkNotNull(this.defaultSpawn, "default cannot be null.");
            if (this.spawns == null) {
                this.spawns = new SpawnList(1);
            }

            if (this.spawns.isEmpty()) {
                this.spawns.add(this.defaultSpawn);
            }
            return new SpawnsModule(moduleManager, this);
        }
    }

    public static class Commands {

        @Command(aliases = {"spawn"}, desc = "spawn")
        public void spawn(CommandSender sender) {
            BlurPlayer blurPlayer = BlurPlugin.get().getBlur().getPlayer(((Player) sender));
            SpawnsModule spawnsModule = blurPlayer.getSession().getModule(SpawnsModule.class).get(0);
            spawnsModule.spawnPlayer(blurPlayer);
        }

    }
}
