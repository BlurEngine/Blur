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

package com.blurengine.blur.utils

import com.blurengine.blur.session.BlurPlayer
import com.supaham.commons.bukkit.utils.ImmutableVector
import com.supaham.commons.minecraft.world.space.Position
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

/* ================================
 * >> Space
 * ================================ */

fun Vector.toPosition(yaw: Float = 0F, pitch: Float = 0F) = Position(x, y, z, yaw, pitch)

fun ImmutableVector.toPosition(yaw: Float = 0F, pitch: Float = 0F) = Position(x, y, z, yaw, pitch)

fun Location.toPosition() = Position(x, y, z, yaw, pitch)

fun Location.toSupaVector() = com.supaham.commons.minecraft.world.space.Vector(x, y, z)

fun Position.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)

/* ================================
 * >> Player
 * ================================ */

fun BlurPlayer.getTeam() = session.moduleManager.teamManager.getPlayerTeam(this)
