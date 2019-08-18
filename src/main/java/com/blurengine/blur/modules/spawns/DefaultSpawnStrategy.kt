/*
 * Copyright 2019 Ali Moghnieh
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

package com.blurengine.blur.modules.spawns

import org.bukkit.entity.Entity
import java.util.function.Supplier


class DefaultSpawnStrategy(override val spawns: Supplier<Collection<Spawn>>, val defaultSpawn: Spawn) : SpawnStrategy {
    override fun getSpawn(entity: Entity): Spawn? {
        val spawns = spawns.get()
        return spawns.firstOrNull { s -> s.filter.test(entity).isAllowed } ?: defaultSpawn
    }
}
