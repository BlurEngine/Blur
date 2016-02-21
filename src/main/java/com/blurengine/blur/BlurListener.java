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

package com.blurengine.blur;

import com.blurengine.blur.events.players.PlayerMoveBlockEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.utils.EventUtils;
import com.supaham.commons.bukkit.utils.LocationUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

class BlurListener implements Listener {

    private final BlurPlugin plugin;

    public BlurListener(BlurPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void callPlayerMoveBlockEvent(PlayerMoveEvent event) {
        if (!LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
            BlurPlayer blurPlayer = plugin.getBlur().getPlayer(event.getPlayer());
            if (blurPlayer.getSession() != null) {
                EventUtils.callEvent(new PlayerMoveBlockEvent(event, blurPlayer));
            }
        }
    }
}
