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

import com.blurengine.blur.events.players.PlayerDamagePlayerEvent;
import com.blurengine.blur.events.players.PlayerMoveBlockEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.utils.EventUtils;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/*
 * All events extend LOW to call Blur events at an early enough point in time that other code will be able to cancel these Bukkit events if necessary.
 */
class BlurListener implements Listener {

    private final BlurPlugin plugin;

    public BlurListener(BlurPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * Override default bukkit behaviour which cancels interact event when clicking air.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().name().endsWith("AIR")) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void callPlayerMoveBlockEvent(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (!from.toVector().equals(to.toVector())) {
            BlurPlayer blurPlayer = plugin.getBlur().getPlayer(event.getPlayer());
            if (blurPlayer.getSession() != null) {
                EventUtils.callEvent(new PlayerMoveBlockEvent(event, blurPlayer));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void callPlayerDamagePlayerEvent(EntityDamageByEntityEvent event) {
        LivingEntity damager = EventUtils.getLivingEntityDamager(event);
        if (event.getEntity() instanceof Player && damager instanceof Player) {
            BlurPlayer blurDamager = plugin.getBlur().getPlayer((Player) damager);
            BlurPlayer blurVictim = plugin.getBlur().getPlayer((Player) event.getEntity());

            PlayerDamagePlayerEvent damageEvent = new PlayerDamagePlayerEvent(blurDamager, blurVictim, event);
            blurDamager.getSession().callEvent(damageEvent);
            if (damageEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }
}
