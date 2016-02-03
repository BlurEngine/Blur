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

import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.framework.WorldModule;
import com.blurengine.blur.modules.spawns.SpawnsModule.SpawnsData;
import com.blurengine.blur.serializers.SpawnList;
import com.blurengine.blur.session.BlurPlayer;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.Map;

import pluginbase.config.annotation.Name;

@ModuleInfo(name = "Spawns", dataClass = SpawnsData.class)
public class SpawnsModule extends WorldModule {

    private final SpawnsData data;

    public SpawnsModule(ModuleManager moduleManager, SpawnsData data) {
        super(moduleManager);
        this.data = data;
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
            } else {
                spawnPlayer(event.getBlurPlayer(), data.spawnOnStart == null ? data.defaultSpawn : data.spawnOnStart);
            }
        }
    }

    public void spawnPlayer(BlurPlayer blurPlayer) {
        spawnPlayer(blurPlayer, getNextSpawn());
    }

    public void spawnPlayer(BlurPlayer blurPlayer, Spawn spawn) {
        Location location = spawn.getExtent().getRandomLocation().toLocation(getSession().getWorld());
        getLogger().finer("Spawning %s at %s", blurPlayer.getName(), location);
        blurPlayer.teleport(location);
    }

    public Spawn getNextSpawn() {
        return data.spawns.stream().findAny().orElse(null);
    }

    public static final class SpawnsData implements ModuleData {

        @Name("default")
        private Spawn defaultSpawn;
        private SpawnList spawns = new SpawnList();
        @Name("spawn-on-start")
        private Spawn spawnOnStart;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof Map) {
                Map<String, Object> asMap = serialized.getAsMap();

                // The following code is a layer of convenience for the user input
                // if input is null or true set to 'default', if 'false' set to null, meaning don't spawn players on start.
                Object o = asMap.get("spawn-on-start");
                if (o == null || o instanceof Boolean) {
                    setSpawnOnStart(asMap, (Boolean) o);
                } else if (o instanceof String) {
                    String s = o.toString().trim();
                    if (s.isEmpty()) {
                        setSpawnOnStart(asMap, null);
                    }
                    if ("false".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s)) {
                        setSpawnOnStart(asMap, Boolean.parseBoolean(o.toString()));
                    }
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
                this.spawns = new SpawnList();
            }

            if (this.spawns.isEmpty()) {
                this.spawns.add(this.defaultSpawn);
            }
            return new SpawnsModule(moduleManager, this);
        }

        private void setSpawnOnStart(Map<String, Object> map, Boolean b) {
            if (b == null) {
                map.put("spawn-on-start", "default");
            } else if (b) {
                map.put("spawn-on-start", "default");
            } else {
                map.remove("spawn-on-start");
            }
        }
    }
}
