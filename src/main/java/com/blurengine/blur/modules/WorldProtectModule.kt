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

package com.blurengine.blur.modules

import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.modules.WorldProtectModule.WorldProtectData
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.data.type.CaveVinesPlant
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOWEST
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityBreakDoorEvent
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.player.PlayerSignOpenEvent
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.event.world.WorldEvent
import org.bukkit.inventory.ItemStack
import pluginbase.config.annotation.Name

@ModuleInfo(name = "WorldProtect", dataClass = WorldProtectData::class)
class WorldProtectModule(moduleManager: ModuleManager, val data: WorldProtectData) : WorldModule(moduleManager) {
    private fun Event.test(bool: Boolean): Boolean {
        val world = when (this) {
            is WorldEvent -> world
            is EntityEvent -> entity.world
            is PlayerEvent -> player.world
            is BlockEvent -> block.world
            is WeatherEvent -> world
            else -> null
        }
        if (world != null && getWorld() != world) {
            return false
        }
        return bool
    }

    private fun Entity.isCreative() = this is Player && this.gameMode == GameMode.CREATIVE

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!event.player.isCreative() && event.test(data.blockBreak)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!event.player.isCreative() && event.test(data.blockPlace)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        if ((event.ignitingEntity !is Player || !(event.ignitingEntity as Player).isCreative()) && event.test(data.blockIgnite)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onFireDestroy(event: BlockBurnEvent) {
        if (event.test(data.blockBurn)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockFade(event: BlockFadeEvent) {
        if (event.test(data.blockFade)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockForm(event: BlockFormEvent) {
        if (event.test(data.blockForm)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockFromTo(event: BlockFromToEvent) {
        if (event.test(data.blockFromTo)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBlockGrow(event: BlockGrowEvent) {
        if (event.test(data.blockGrow)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onCactusPhysics(event: BlockPhysicsEvent) {
        if (event.test(data.cactusPhysics) && event.block.type == Material.CACTUS) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onLeavesDecay(event: LeavesDecayEvent) {
        if (event.test(data.leavesDecay)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onEntityCombust(event: EntityCombustEvent) {
        if (event.test(data.entityCombust)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (event.test(data.entityExplode)) event.blockList().clear()
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onEntityBreakDoor(event: EntityBreakDoorEvent) {
        if (!event.entity.isCreative() && event.test(data.entityBreakDoor)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (!event.entity.isCreative() && event.test(data.playerDropItem)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        if (event.test(data.creatureSpawn)) {
            val allow = when (event.spawnReason) {
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                CreatureSpawnEvent.SpawnReason.SPAWNER_EGG -> {
                    true
                }
                else -> false
            }
            if (!allow) {
                event.entity.remove()
            }
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onHangingBreak(event: HangingBreakEvent) {
        if (event.entity.isCreative()) return
        if (event.test(data.hangingBreak)) {
            event.isCancelled = true
        } else if (event.test(data.hangingBreakByPlayer) && event is HangingBreakByEntityEvent && event.remover is Player) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onHangingPlace(event: HangingPlaceEvent) {
        if (!event.player!!.isCreative() && event.test(data.hangingPlace)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (!event.player.isCreative()) {
            event.isCancelled = when {
                event.rightClicked is ItemFrame -> event.test(data.hangingInteract)
                else -> false
            }
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onEntityDamageByPlayer(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        fun checkPlayer(damager: Player) {
            if (!damager.isCreative()) {
                event.isCancelled = when {
                    event.entity is ItemFrame -> event.test(data.hangingInteract)
                    else -> event.test(data.playerDamageEntity)
                }

            }
        }

        if (damager is Player) {
            checkPlayer(damager)
        } else if (damager is Projectile) {
            val shooter = damager.shooter
            if (shooter is Player) {
                checkPlayer(shooter)
            }
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onVehicleDamage(event: VehicleDamageEvent) {
        if (event.attacker?.isCreative() == true) return
        if (event.test(data.vehicleDamage)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        if (event.attacker?.isCreative() == true) return
        if (event.test(data.vehicleDestroy)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock() && !event.player.isCreative() && event.test(data.interactBlock)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        if (event.view.player.isCreative()) return
        if (event.test(data.itemCraft)) event.inventory.result = ItemStack(Material.AIR)
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerBedEnter(event: PlayerBedEnterEvent) {
        if (!event.player.isCreative() && event.test(data.playerBedEnter)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (!event.player.isCreative() && event.test(data.bucketEmpty)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onBucketFill(event: PlayerBucketFillEvent) {
        if (!event.player.isCreative() && event.test(data.bucketFill)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        if (!event.player.isCreative() && event.test(data.shearEntity)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onWeatherChange(event: WeatherChangeEvent) {
        if (event.test(data.weatherChange)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onCleanUpArrows(event: ProjectileHitEvent) {
        if (event.test(data.cleanUpArrows) && event.entity.type == EntityType.ARROW) event.entity.remove()
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!event.player.isCreative() && event.test(data.playerDropItem)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerPickBerries(event: PlayerInteractEvent) {
        if ((event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock!!.blockData is CaveVinesPlant) ||
                (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock!!.blockData.material == Material.SWEET_BERRY_BUSH)) {
            if (!event.player.isCreative() && event.test(data.pickBerries)) event.isCancelled = true
        }
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        if (!event.player.isCreative() && event.test(data.armorStandManipulate)) event.isCancelled = true
    }

    @EventHandler(priority = LOWEST, ignoreCancelled = true)
    fun onPlayerSignEdit(event: PlayerSignOpenEvent) {
        if (!event.player.isCreative() && event.test(data.signEdit)) event.isCancelled = true
    }

    class WorldProtectData : ModuleData {
        @Name("block-break")
        var blockBreak: Boolean = true
        @Name("block-place")
        var blockPlace: Boolean = true
        @Name("block-ignite")
        var blockIgnite: Boolean = true
        @Name("block-burn")
        var blockBurn: Boolean = true
        @Name("block-fade")
        var blockFade: Boolean = true
        @Name("block-form")
        var blockForm: Boolean = true
        @Name("block-from-to")
        var blockFromTo: Boolean = true
        @Name("block-grow")
        var blockGrow: Boolean = true
        @Name("cactus-physics")
        var cactusPhysics: Boolean = true
        @Name("leaves-decay")
        var leavesDecay: Boolean = true

        @Name("entity-combust")
        var entityCombust: Boolean = true
        @Name("entity-explode")
        var entityExplode: Boolean = true
        @Name("entity-break-door")
        var entityBreakDoor: Boolean = true
        @Name("entity-pickup-item")
        var entityPickupItem: Boolean = true
        @Name("creature-spawn")
        var creatureSpawn: Boolean = true
        @Name("hanging-break")
        var hangingBreak: Boolean = true
        @Name("hanging-break-by-player")
        var hangingBreakByPlayer: Boolean = true
        @Name("hanging-place")
        var hangingPlace: Boolean = true
        @Name("hanging-interact")
        var hangingInteract: Boolean = true
        @Name("vehicle-damage")
        var vehicleDamage: Boolean = true
        @Name("vehicle-destroy")
        var vehicleDestroy: Boolean = true

        @Name("interact-block")
        var interactBlock: Boolean = false
        @Name("pick-berries")
        var pickBerries: Boolean = true
        @Name("item-craft")
        var itemCraft: Boolean = true
        @Name("player-bed-enter")
        var playerBedEnter: Boolean = true
        @Name("bucket-empty")
        var bucketEmpty: Boolean = true
        @Name("bucket-fill")
        var bucketFill: Boolean = true
        @Name("shear-entity")
        var shearEntity: Boolean = true
        @Name("player-drop-item")
        var playerDropItem: Boolean = true
        @Name("player-damage-entity")
        var playerDamageEntity: Boolean = true
        @Name("armor-stand-manipulate")
        var armorStandManipulate: Boolean = true
        @Name("sign-edit")
        var signEdit: Boolean = true

        @Name("weather-change")
        var weatherChange: Boolean = true

        @Name("clean-up-arrows")
        var cleanUpArrows: Boolean = true

        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            serialized.load(this)
            return WorldProtectModule(moduleManager, this)
        }
    }
}
