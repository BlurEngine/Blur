package com.blurengine.blur.modules.spawns

import org.bukkit.entity.Entity
import java.util.function.Supplier


class DefaultSpawnStrategy(override val spawns: Supplier<Collection<Spawn>>, val defaultSpawn: Spawn) : SpawnStrategy {
    override fun getSpawn(entity: Entity): Spawn? {
        val spawns = spawns.get()
        return spawns.firstOrNull { s -> s.filter.test(entity).isAllowed } ?: defaultSpawn
    }
}
