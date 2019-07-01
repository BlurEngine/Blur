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

package com.blurengine.blur

import com.blurengine.blur.events.players.PlayerDamagePlayerEvent
import com.blurengine.blur.events.players.PlayerMoveBlockEvent
import com.supaham.commons.bukkit.utils.EventUtils
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.PlayerInventory

/*
 * All events extend LOW to call Blur events at an early enough point in time that other code will be able to cancel these Bukkit events if necessary.
 */
internal class BlurListener(private val plugin: BlurPlugin) : Listener {

    /*
     * Override default bukkit behaviour which cancels interact event when clicking air.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action.name.endsWith("AIR")) {
            event.isCancelled = false
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun callPlayerMoveBlockEvent(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to
        if (from.toVector() != to!!.toVector()) {
            val blurPlayer = plugin.blur.getPlayer(event.player)
            if (blurPlayer != null && blurPlayer.session != null) {
                EventUtils.callEvent(PlayerMoveBlockEvent(event, blurPlayer))
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun callPlayerDamagePlayerEvent(event: EntityDamageByEntityEvent) {
        val damager = EventUtils.getLivingEntityDamager(event)
        if (event.entity is Player && damager is Player) {
            val blurDamager = plugin.blur.getPlayer(damager)
            val blurVictim = plugin.blur.getPlayer(event.entity as Player)
            if (blurDamager == null || blurVictim == null) {
                plugin.log.info("Bad damager/victim damager=%s | victim=%s", blurDamager, blurVictim)
                return
            }

            val damageEvent = PlayerDamagePlayerEvent(blurDamager, blurVictim, event)
            blurDamager.session.callEvent(damageEvent)
            if (damageEvent.isCancelled) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun preventUnclickableInventoryClicks(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) {
            return
        }
        val player = event.whoClicked as Player
        val blurPlayer = plugin.blur.getPlayer(player) ?: return
        val invLayout = blurPlayer.coreData.inventoryLayout

        if (event.slot in 0..35 && event.clickedInventory is PlayerInventory) {
            var clickable = true
            if (!invLayout.getTypeBySlot(event.slot).isClickable) {
                clickable = false
            } else if (event.action == InventoryAction.HOTBAR_SWAP || event.action == InventoryAction.HOTBAR_MOVE_AND_READD) {
                // Check, if hotbar destination slot is clickable.
                clickable = invLayout.getTypeBySlot(event.hotbarButton).isClickable
            }
            event.isCancelled = !clickable
            return
        }
    }
}
