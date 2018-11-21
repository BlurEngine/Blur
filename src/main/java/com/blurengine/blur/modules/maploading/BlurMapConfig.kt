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

package com.blurengine.blur.modules.maploading

import com.blurengine.blur.properties.BlurConfig
import com.github.zafarkhaja.semver.Version
import com.supaham.commons.bukkit.serializers.ColorStringSerializer
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import pluginbase.config.annotation.Name
import pluginbase.config.annotation.SerializeWith
import java.util.UUID

class BlurMapConfig : BlurConfig() {
    var map: MapData? = null
        private set

    class MapData {

        var version: Version? = null
            private set
        @SerializeWith(ColorStringSerializer::class)
        var name: String? = null
            private set
        @SerializeWith(ColorStringSerializer::class)
        var description: String? = null
            private set
        var authors: List<Author>? = null
            private set
        @Name("min-players")
        var minPlayers: Int = 0
            private set
        @Name("max-players")
        var maxPlayers: Int = Int.MAX_VALUE
            private set
        @Name("world-settings")
        var worldSettings = WorldSettings()
            private set

        fun checkValues() {
            if (minPlayers > 0) {
                require(minPlayers <= maxPlayers) { "minPlayers cannot be greater than maxPlayers." }
            }
        }
    }

    class Author {
        var uuid: UUID? = null
            private set
        var role: String? = null
            private set
    }

    class WorldSettings {
        var seed: String? = null
            private set
        var environment: World.Environment? = World.Environment.NORMAL
            private set
        var type: WorldType? = WorldType.NORMAL
            private set
        var generatorSettings: String = ""
            private set
        var generateStructures: Boolean = true
            private set

        // Do not allow name as a setting as that is defined via map loader

        fun toWorldCreator(name: String) = WorldCreator(name)
                .environment(environment)
                .type(type)
                .generatorSettings(generatorSettings)
                .generateStructures(generateStructures)
                .apply {
                    if (seed != null) {
                        try {
                            seed(seed!!.toLong())
                        } catch (e: NumberFormatException) {
                            seed(seed!!.hashCode().toLong())
                        }
                    }
                }

    }
}
