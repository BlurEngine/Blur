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

import com.blurengine.blur.framework.ModuleLoader
import com.supaham.commons.bukkit.utils.SerializationUtils
import pluginbase.config.datasource.yaml.YamlDataSource
import java.io.File
import java.io.IOException

/**
 * Represents a Map that consists of a [File] representing a [com.blurengine.blur.properties.BlurConfig].
 */
class BlurMap {
    lateinit var mapLoader: MapLoaderModule
        private set
    lateinit var mapDirectory: File
        private set
    lateinit var mapFile: File
        private set
    val config: BlurMapConfig by lazy {
        val yaml: YamlDataSource
        try {
            if (!mapFile.exists()) {
                throw MapLoadException(MAP_FILE_NAME + " is missing in " + mapFile.toString())
            }
            yaml = SerializationUtils.yaml(mapFile).build()
        } catch (e: IOException) {
            throw MapLoadException("Failed to read " + MAP_FILE_NAME, e)
        }

        val config = BlurMapConfig()
        SerializationUtils.loadOrCreateProperties(mapLoader.logger, yaml, config, null, ModuleLoader.getStaticSerializerSetBuilder().build())
        require(config.map != null) { "map section must be defined in $id" }
        return@lazy config
    }

    val id: String

    companion object {
        val MAP_FILE_NAME = "blur.yml"
    }

    constructor(mapLoader: MapLoaderModule, mapDirectory: File) : this(mapLoader, mapDirectory, File(mapDirectory, MAP_FILE_NAME))

    constructor(mapLoader: MapLoaderModule, mapDirectory: File, mapFile: File) {
        this.mapLoader = mapLoader
        this.mapDirectory = mapDirectory
        this.mapFile = mapFile
        require(mapFile.exists()) { "${mapFile.path} does not exist." }
        this.id = mapDirectory.name
    }
}
