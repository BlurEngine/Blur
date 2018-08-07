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

import com.blurengine.blur.Blur;
import com.blurengine.blur.events.players.BlurPlayerDeathEvent;
import com.blurengine.blur.framework.Component;
import com.blurengine.blur.framework.Module;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

/**
 * General {@link BlurSession} listener for handling and calling events commonly prior to them reaching {@link Component}s and {@link Module}s
 * themselves. 
 */
public class BlurSessionListener implements Listener {

    private final BlurSession session;

    public BlurSessionListener(BlurSession session) {
        this.session = session;
    }

    private boolean isSession(BlurPlayer blurPlayer) {
        return blurPlayer != null && isSession(blurPlayer.getSession());
    }

    private boolean isSession(BlurSession blurSession) {
        return this.session.equals(blurSession);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        BlurPlayer blurPlayer = getPlayer(event.getEntity()).orElse(null);
        if (isSession(blurPlayer)) {
            blurPlayer.die();
        }
    }

    // Priority HIGH is crucial as the BlurPlayer reference is disposed of in HIGHEST
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        BlurPlayer blurPlayer = session.getBlur().getPlayer(event.getPlayer());
        session.removePlayer(blurPlayer, true);
    }

    private Optional<BlurPlayer> getPlayer(Entity entity) {
        if (entity == null || !(entity instanceof Player)) {
            return Optional.empty();
        }
        return Optional.of(getBlur().getPlayer((Player) entity));
    }

    private Blur getBlur() {
        return this.session.getBlur();
    }
}
