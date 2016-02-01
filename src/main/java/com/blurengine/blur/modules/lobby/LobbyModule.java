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
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.lobby.LobbyModule.LobbyData;
import com.blurengine.blur.modules.maploading.MapLoadException;
import com.blurengine.blur.modules.maploading.MapLoaderModule;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.session.WorldBlurSession;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import pluginbase.config.annotation.Name;

@ModuleInfo(name = "Lobby", dataClass = LobbyData.class)
public class LobbyModule extends Module {

    private final LobbyData data;
    private final List<BlurSession> childrenSessions = new ArrayList<>();
    protected AbstractCountdown countdown;

    public LobbyModule(ModuleManager moduleManager, LobbyData data) {
        super(moduleManager);
        this.data = data;
        if (!data.countdown.isZero()) {
            this.countdown = new LobbyCountdown();
        }
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
    public void onPlayerJoinSession(PlayerJoinSessionEvent event) {
        if (isSession(event)) {
            checkAndStart();
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

            // Add players to the new session immediately
            getSession().getPlayersStream().forEach(childSession::addPlayer);

            // Make the wheels on the bus go round and round.
            childSession.start();
            this.childrenSessions.add(childSession);
        } catch (MapLoadException e) {
            e.printStackTrace();
            getSession().stop(); // Stop session because of the map load failure.
        }
    }

    @Override
    public WorldBlurSession getSession() {
        return ((WorldBlurSession) super.getSession());
    }

    public static final class LobbyData implements ModuleData {

        private Duration countdown = Duration.ZERO;
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
            super(LobbyModule.this, (int) (data.countdown.toMillis() / 50));
        }

        @Override
        public void onEnd() {
            super.onEnd();
            startNextSession();
        }
    }
}
