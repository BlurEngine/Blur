/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.modules.vanillafixes

import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileLaunchEvent

/**
 * Fixes Minecraft 1.9 y projectile addition to projectile throwing.
 * @author Elijah "Xorgon" Andrews
 */
@ModuleInfo(name = "FixProjectileVelocity")
class FixProjectileVelocity(moduleManager: ModuleManager) : Module(moduleManager) {

    @EventHandler
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val entity = event.entity
        if (entity.shooter is Player) {
            val player = entity.shooter as Player
            val speed = entity.velocity.length()
            entity.velocity = player.location.direction.multiply(speed)
        }
    }
}
