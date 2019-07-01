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

package com.blurengine.blur.modules.maploading

import com.blurengine.blur.events.session.SessionStartEvent
import com.blurengine.blur.events.session.SessionStopEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.ModuleParseException
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.modules.filters.Filter
import com.blurengine.blur.modules.maploading.MapLoaderModule.MapLoaderData
import com.blurengine.blur.session.WorldBlurSession
import com.google.common.base.Preconditions
import com.supaham.commons.Joiner
import com.supaham.commons.utils.CollectionUtils
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import pluginbase.config.annotation.Name
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Stream

/**
 * Represents a [Module] that allows for the creation of [Filter] and nothing else. Intended for user convenience.
 */
@ModuleInfo(name = "MapLoader", dataClass = MapLoaderData::class)
class MapLoaderModule(moduleManager: ModuleManager, val rootDirectory: File, mapPaths: Set<File>, val isRandom: Boolean, archive: Archive?)
    : Module(moduleManager) {
    private val mapPaths: List<File>
    private val archiver: LocalArchiver?

    private val sessions = LinkedHashMap<WorldBlurSession, BlurMap>()

    var nextMapStrategy: MapChoiceStrategy = DefaultMapChoiceStrategy()

    init {
        this.mapPaths = ArrayList(mapPaths)
        logger.fine("Found maps %s", Joiner.on(", ").function<Any> { f -> (f as File).name }.join(mapPaths))
        this.archiver = if (archive == null) null else LocalArchiver(this, archive)
    }

    override fun unload() {
        super.unload()
        ArrayList(this.sessions.keys).forEach { this.unloadMap(it) }
    }

    @EventHandler
    fun onSessionStart(event: SessionStartEvent) {
        if (!isSession(event)) return

        if (session.callEvent(MapLoaderPreLoadEvent(this)).isCancelled) return

        if (this.mapPaths.isEmpty()) {
            logger.warning("No maps to load!")
            return
        }

        try {
            val map = nextMap()
            val newSession = createSessionFromDirectory(map)
            newSession.start()
            logger.fine("Next map " + map.id)
        } catch (e: MapLoadException) {
            e.printStackTrace()
        }

    }

    private fun unloadMap(session: WorldBlurSession): Boolean {
        // Does the given session belong to us?
        if (this.sessions.remove(session) == null) {
            return false // Probably a case where the session was unloaded already
        }
        logger.fine("Unloading %s from MapLoader.", session.name)
        val world = session.world
        world.players.forEach { player -> player.teleport(Bukkit.getWorlds()[0].spawnLocation) } // TODO change fixed world.
        require(Bukkit.unloadWorld(world, true)) { "Failed to unload world ${world.name}"}
        var canDelete = true

        val worldFolder = world.worldFolder
        if (this.archiver != null) {
            canDelete = this.archiver.archive(worldFolder)
        }

        if (canDelete) {
            try {
                logger.fine("Deleting stale map: %s", worldFolder.path)
                FileUtils.deleteDirectory(worldFolder)
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "Failed to delete " + worldFolder.path, e)
                // Don't return false here as the map has been unloaded successfully for the most relevant part. This error needs to be handled
                // in a different way.
            }

        }
        return true
    }

    @EventHandler
    fun onSessionStop(event: SessionStopEvent) {
        val session = event.session
        if (session is WorldBlurSession && this.sessions.containsKey(session)) {
            session.addOnStopTask { unloadMap(session) }
        }
    }

    // To async or not to async?
    @Throws(MapLoadException::class)
    fun createSessionFromDirectory(map: BlurMap): WorldBlurSession {
        Preconditions.checkNotNull(map, "map")
        val worldName = GENERATED_WORLD_DIRECTORY_PREFIX + map.id

        val worldDir = File(Bukkit.getWorldContainer(), worldName)
        if (worldDir.exists()) {
            try {
                logger.fine("Deleting stale map: %s", worldDir.path)
                FileUtils.deleteDirectory(worldDir)
            } catch (e: IOException) {
                throw MapLoadException("Failed to delete old directory: " + map.mapDirectory.path, e)
            }
        }
        try {
            logger.fine("Copying %s to directory: %s", map.mapDirectory.path, worldDir.path)
            FileUtils.copyDirectory(map.mapDirectory, worldDir)
        } catch (e: IOException) {
            throw MapLoadException("Failed to duplicate: " + map.mapDirectory.path, e)
        }

        val worldCreator = map.config.map!!.worldSettings.toWorldCreator(worldDir.name)

        val world = worldCreator.createWorld()

        // Create and load map config
        val newSession = session.addChildSession(WorldBlurSession(session, world!!, map))
        sessions.put(newSession, map)
        newSession.name = map.id
        newSession.moduleManager.moduleLoader.load(map.config.modules)
        return newSession
    }

    fun nextMap(): BlurMap {
        val map = nextMapStrategy.getMap()
        val event = session.callEvent(ChooseNextMapEvent(this, map))
        return event.nextMap
    }

    fun getMapPaths(): List<File> {
        return Collections.unmodifiableList(mapPaths)
    }

    val blurMaps: List<BlurMap> get() = getMapPaths().map { file -> BlurMap(this, file) }

    fun getSessions(): Map<WorldBlurSession, BlurMap> {
        return Collections.unmodifiableMap(this.sessions)
    }

    fun getBlurMap(session: WorldBlurSession) = sessions[session]

    class MapLoaderData : ModuleData {

        private val mapPaths = LinkedHashSet<File>()
        private var directory: File? = null
        private var archive: Archive? = null

        @Throws(ModuleParseException::class)
        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module {
            Preconditions.checkNotNull(serialized.asObject, "MapLoader data cannot be null.")

            val map = serialized.asMap
            val directoryString = map["directory"]
            val rawMaps = checkNotNull(map["maps"], "MapLoader missing maps property.")

            if (directoryString !is String) {
                this.directory = File(".")
            } else {
                this.directory = File(directoryString.toString())
                check(directory!!.exists(), "%s does not exist.", directory)
                check(directory!!.isDirectory, "%s must be a directory.", directory)
            }

            // Direct list of map (directory) names
            if (rawMaps is String) { // Single map in the form of a string
                addStrings(moduleManager.logger, Stream.of(rawMaps as String))
            } else if (rawMaps is List<*>) {
                addStrings(moduleManager.logger, (rawMaps as List<String>).stream())
            } else if (rawMaps is Map<*, *>) {
                // map of key 'file' with the path to the file that contains maps.
                val _map = rawMaps as Map<String, Any>
                var fileString = checkNotNull<Any>(_map["file"], "MapLoader maps missing file property.").toString()
                fileString = fileString.replace("\\{directory\\}".toRegex(), this.directory!!.toString())

                val file = File(fileString)
                check(file.exists(), "%s does not exist.", file)
                check(file.isFile, "%s must be a file.", file)

                try {
                    BufferedReader(FileReader(file)).use { br -> addStrings(moduleManager.logger, br.lines()) }
                } catch (e: IOException) {
                    throw ModuleParseException(e.message)
                }

            }
            val random = map["random"]
            val randomBool = if (random == null) false else java.lang.Boolean.valueOf(random.toString())

            if (map.containsKey("archive")) {
                val o = map["archive"]
                this.archive = null // If archive is present, assume we must start archives, unless value is "false". See if-string.

                if (o is String) {
                    // Check for true and false to toggle archiving.
                    if (o.toString().equals("true", ignoreCase = true) || o.toString().equals("false", ignoreCase = true)) {
                        // Only if the value is true do we disable archiving.
                        if (java.lang.Boolean.parseBoolean(o.toString())) {
                            this.archive = Archive()
                        }
                    } else {
                        this.archive = Archive()
                        this.archive!!.directory = o.toString()
                    }
                } else if (o is Map<*, *>) { // Automatically load from class fields
                    this.archive = Archive()
                    moduleManager.moduleLoader.deserializeTo(o, archive!!)

                    // Type-safe compression type.
                    val compress = this.archive!!.compress
                    if (compress == null) {
                        this.archive!!.compressionTypeEnum = ArchiveCompressionType.NONE
                    } else {
                        // Boolean true = ZIP, false = NONE
                        if (compress.equals("true", ignoreCase = true) || compress.equals("false", ignoreCase = true)) {
                            this.archive!!.compressionTypeEnum = if (java.lang.Boolean.valueOf(compress)) ArchiveCompressionType.ZIP else ArchiveCompressionType.NONE
                        } else {
                            try {
                                this.archive!!.compressionTypeEnum = ArchiveCompressionType.valueOf(compress.toUpperCase())
                            } catch (e: IllegalArgumentException) {
                                check(false, "%s is not a valid compression type. Compression types: %s", compress,
                                        Joiner.on(',').join(ArchiveCompressionType.values()))
                            }

                        }
                    }
                }
            }

            return MapLoaderModule(moduleManager, this.directory!!, this.mapPaths, randomBool, archive)
        }

        @Throws(ModuleParseException::class)
        private fun addStrings(logger: Logger, paths: Stream<String>) {
            val files = paths.map { line -> File(this.directory, line) }

            /* ================================
             * >> /Informative Data
             * ================================ */
            val _inexistant = ArrayList<File>()
            val _nonDirs = ArrayList<File>()
            val _nonMC = ArrayList<File>()
            for (file in files) {
                when (addMapDir(file)) {
                    // This line adds the file directory to this.mapPaths
                    1 -> _inexistant.add(file)
                    2 -> _nonDirs.add(file)
                    3 -> _nonMC.add(file)
                }
            }
            if (_inexistant.size > 0) {
                logger.severe("The following directories do not exist: " + Joiner.on(", ").join(_inexistant))
            }
            if (_nonDirs.size > 0) {
                logger.severe("The following files must be directories: " + Joiner.on(", ").join(_nonDirs))
            }
            if (_nonMC.size > 0) {
                logger.severe("The following directories are not Minecraft worlds: " + Joiner.on(", ").join(_nonMC))
            }
            /* ================================
             * >> /Informative Data
             * ================================ */
        }

        @Throws(ModuleParseException::class)
        private fun addMapDir(path: String): Int {
            return addMapDir(File(this.directory, path))
        }

        @Throws(ModuleParseException::class)
        private fun addMapDir(file: File): Int {
            if (!file.exists()) {
                return 1
            }
            if (!file.isDirectory) {
                return 2
            }
            if (!isMinecraftWorldDir(file)) {
                return 3
            }
            mapPaths.add(file)
            return 0
        }

        private fun isMinecraftWorldDir(file: File): Boolean {
            return File(file, "level.dat").exists()
                    && (File(file, "DIM1/region").exists() || File(file, "DIM-1/region").exists() || File(file, "region").exists())
        }
    }

    class Archive {

        var directory = "./archives"
        var compress: String? = "true"
        @Name("name-template")
        var nameTemplate = "{mapname}-{datetime}"

        // compressionType set from parse method above, represents compressing value.
        @Transient
        var compressionTypeEnum = ArchiveCompressionType.ZIP
    }

    companion object {
        val GENERATED_WORLD_DIRECTORY_PREFIX = "blur_"
    }

    inner class DefaultMapChoiceStrategy : MapChoiceStrategy {
        private var nextIndex = 0

        override fun getAvailableMaps(): List<BlurMap> = blurMaps

        override fun getMap(): BlurMap {
            val paths = getMapPaths()

            val file: File
            if (isRandom) {
                file = CollectionUtils.getRandomElement(paths)
            } else {
                val index = nextIndex
                file = paths[index]
                if (++nextIndex > this@MapLoaderModule.mapPaths.lastIndex) nextIndex = 0
            }
            return BlurMap(this@MapLoaderModule, file)
        }
    }
}
