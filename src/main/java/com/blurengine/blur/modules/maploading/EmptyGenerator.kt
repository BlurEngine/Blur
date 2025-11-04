package com.blurengine.blur.modules.maploading

import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class EmptyGenerator : ChunkGenerator() {
    override fun shouldGenerateNoise(): Boolean {
        return false
    }

    override fun shouldGenerateNoise(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }

    override fun shouldGenerateSurface(): Boolean {
        return false
    }

    override fun shouldGenerateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }

    override fun shouldGenerateCaves(): Boolean {
        return false
    }

    override fun shouldGenerateCaves(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }

    override fun shouldGenerateDecorations(): Boolean {
        return false
    }

    override fun shouldGenerateDecorations(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }

    override fun shouldGenerateMobs(): Boolean {
        return false
    }

    override fun shouldGenerateMobs(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }

    override fun shouldGenerateStructures(): Boolean {
        return false
    }

    override fun shouldGenerateStructures(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return false
    }
}