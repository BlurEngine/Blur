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

package com.blurengine.blur.modules.lobby;

import com.blurengine.blur.countdown.AbstractCountdown;
import com.blurengine.blur.countdown.GlobalGameCountdown;
import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent;
import com.blurengine.blur.events.session.SessionStopEvent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.framework.WorldModule;
import com.blurengine.blur.modules.lobby.LobbyModule.LobbyData;
import com.blurengine.blur.modules.maploading.MapLoadException;
import com.blurengine.blur.modules.maploading.MapLoaderModule;
import com.blurengine.blur.modules.maploading.MapLoaderPreLoadEvent;
import com.blurengine.blur.modules.spawns.SpawnsModule;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.session.WorldBlurSession;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import pluginbase.config.annotation.Name;

@ModuleInfo(name = "Lobby", dataClass = LobbyData.class)
public class LobbyModule extends WorldModule {

    private final LobbyData data;
    private final List<BlurSession> childrenSessions = new ArrayList<>();
    protected AbstractCountdown countdown;

    public LobbyModule(ModuleManager moduleManager, LobbyData data) {
        super(moduleManager);
        this.data = data;
        this.countdown = new LobbyCountdown();
    }

    @EventHandler
    public void onMapLoaderPreLoad(MapLoaderPreLoadEvent event) {
        // Cancel any initial MapLoaderModule loading events since we handle it in LobbyCountdown.
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (data.games == 1) {
            BlurPlayer blurPlayer = getSession().getBlur().getPlayer(event.getPlayer());
            getSession().addPlayer(blurPlayer);
        } else {
            getLogger().warning("LobbyModule can't handle more than one game at a time yet.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BlurPlayer blurPlayer = getSession().getBlur().getPlayer(event.getPlayer());
        if (getPlayers().contains(blurPlayer)) {
            getSession().removePlayer(blurPlayer);
        }
    }

    @EventHandler
    public void onPlayerJoinSession(PlayerJoinSessionEvent event) {
        if (isSession(event)) {
            event.getBlurPlayer().reset();
            checkAndStart();
        }
    }

    @EventHandler
    public void onPlayerLeaveSession(PlayerLeaveSessionEvent event) {
        if (isSession(event)) {
            if (this.countdown != null) {
                this.countdown.stop();
            }
        }
    }

    /*
     * Teleport players to the lobby when a session created by this lobby is shutdown.
     */
    @EventHandler
    public void onSessionStop(SessionStopEvent event) {
        if (getSession().getChildrenSessions().contains(event.getSession())) {
            SpawnsModule spawns = getSession().getModule(SpawnsModule.class).get(0);
            event.getSession().getPlayers().values().forEach(spawns::spawnPlayer);

            // Start countdown immediately.
            event.getSession().addOnStopTask(this::checkAndStart);
        }
    }

    protected void checkAndStart() {
        if (getSession().getPlayers().size() >= data.requiredPlayers) {
            if (this.countdown != null) {
                this.countdown.start();
            } else {
                startNextSession();
            }
        }
    }

    protected void startNextSession() {
        if (this.countdown != null) {
            this.countdown.stop();
        }

        MapLoaderModule mapLoaderModule = getModuleManager().getModule(MapLoaderModule.class).get(0); // FIXME this is a temporary hack
        try {
            WorldBlurSession childSession = mapLoaderModule.createSessionFromDirectory(mapLoaderModule.nextMap());

            // Make the wheels on the bus go round and round.
            childSession.load();
            childSession.enable();

            // Add current lobby players to the new session immediately
            getSession().getPlayersStream().forEach(childSession::addPlayer);

            if (!data.delay.isZero()) {
                newTask(childSession::start).delay(data.delay).build();
            } else {
                childSession.start();
            }
            this.childrenSessions.add(childSession);
        } catch (MapLoadException e) {
            e.printStackTrace();
            getSession().stop(); // Stop session because of the map load failure.
        }
    }

    public static final class LobbyData implements ModuleData {

        private Duration countdown = Duration.ofSeconds(15);
        @Name("delay-start-session")
        private Duration delay = Duration.ZERO;

        @Name("required-players")
        private int requiredPlayers = 1;
        private int games = 1;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            check(games == 1, "Lobby only supports games=1 at the moment. Sorry :(");
            return new LobbyModule(moduleManager, this);
        }
    }

    private final class LobbyCountdown extends GlobalGameCountdown {

        public LobbyCountdown() {
            super(LobbyModule.this, Math.max(1, (int) (data.countdown.toMillis() / 50)));
        }

        @Override
        public void onEnd() {
            super.onEnd();
            startNextSession();
        }
    }
}
