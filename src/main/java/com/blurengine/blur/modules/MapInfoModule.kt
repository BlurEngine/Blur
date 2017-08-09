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

import com.blurengine.blur.modules.MapInfoModule.MapInfoData
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.ModuleParseException
import com.blurengine.blur.framework.SerializedModule
import com.github.zafarkhaja.semver.Version
import com.supaham.commons.bukkit.serializers.ColorStringSerializer
import java.util.UUID

import pluginbase.config.annotation.Name
import pluginbase.config.annotation.SerializeWith

@ModuleInfo(name = "MapInfo", dataClass = MapInfoData::class)
class MapInfoModule(moduleManager: ModuleManager, val data: MapInfoData) : Module(moduleManager) {

    override fun toString() = "MapInfoModule{$data}"

    class MapInfoData : ModuleData {

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

        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            serialized.load(this)
            return MapInfoModule(moduleManager, this)
        }
    }

    class Author {
        var uuid: UUID? = null
        private set
        var role: String? = null
            private set
    }
}
