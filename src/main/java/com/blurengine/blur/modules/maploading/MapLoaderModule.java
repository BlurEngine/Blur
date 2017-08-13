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

package com.blurengine.blur.modules.maploading;

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.events.session.SessionStopEvent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.maploading.MapLoaderModule.MapLoaderData;
import com.blurengine.blur.session.WorldBlurSession;
import com.supaham.commons.Joiner;
import com.supaham.commons.utils.CollectionUtils;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.annotation.Name;

/**
 * Represents a {@link Module} that allows for the creation of {@link Filter} and nothing else. Intended for user convenience.
 */
@ModuleInfo(name = "MapLoader", dataClass = MapLoaderData.class)
public class MapLoaderModule extends Module {

    // TODO abstract all this File usage to support global repository access.

    public static final String GENERATED_WORLD_DIRECTORY_PREFIX = "blur_";

    private final File rootDirectory;
    private final boolean random;
    private final List<File> mapPaths;
    private final LocalArchiver archiver;

    private int lastIndex = 0;
    private Map<WorldBlurSession, BlurMap> sessions = new LinkedHashMap<>();

    private MapLoaderModule(ModuleManager moduleManager, File rootDirectory, Set<File> mapPaths, boolean random, Archive archive) {
        super(moduleManager);
        this.rootDirectory = rootDirectory;
        this.mapPaths = new ArrayList<>(mapPaths);
        getLogger().fine("Found maps %s", Joiner.on(", ").function(f -> ((File) f).getName()).join(mapPaths));
        this.random = random;
        this.archiver = archive == null ? null : new LocalArchiver(this, archive);
    }

    @Override
    public void unload() {
        super.unload();
        new ArrayList<>(this.sessions.keySet()).forEach(this::unloadMap);
    }

    @EventHandler
    public void onSessionStart(SessionStartEvent event) {

        if (getSession().callEvent(new MapLoaderPreLoadEvent(this)).isCancelled()) {
            return;
        }

        if (this.mapPaths.isEmpty()) {
            getLogger().warning("No maps to load!");
            return;
        }

        try {
            BlurMap map = nextMap();
            WorldBlurSession newSession = createSessionFromDirectory(map);
            newSession.start();
            getLogger().fine("Next map " + map.getId());
        } catch (MapLoadException e) {
            e.printStackTrace();
        }
    }

    private boolean unloadMap(WorldBlurSession session) {
        // Does the given session belong to us?
        if (this.sessions.remove(session) == null) {
            return false; // Probably a case where the session was unloaded already
        }
        getLogger().fine("Unloading %s from MapLoader.", session.getName());
        World world = session.getWorld();
        world.getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())); // TODO change fixed world.
        Bukkit.unloadWorld(world, true);
        boolean canDelete = true;

        File worldFolder = world.getWorldFolder();
        if (this.archiver != null) {
            canDelete = this.archiver.archive(worldFolder);
        }

        if (canDelete) {
            try {
                FileUtils.deleteDirectory(worldFolder);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to delete " + worldFolder.getPath(), e);
                // Don't return false here as the map has been unloaded successfully for the most relevant part. This error needs to be handled
                // in a different way.
            }
        }
        return true;
    }

    @EventHandler
    public void onSessionStop(SessionStopEvent event) {
        if (event.getSession() instanceof WorldBlurSession && this.sessions.containsKey(event.getSession())) {
            event.getSession().addOnStopTask(() -> unloadMap((WorldBlurSession) event.getSession()));
        }
    }

    // To async or not to async?
    public WorldBlurSession createSessionFromDirectory(@Nonnull BlurMap map) throws MapLoadException {
        Preconditions.checkNotNull(map, "map");
        String worldName = GENERATED_WORLD_DIRECTORY_PREFIX + map.getId();

        File worldDir = new File(Bukkit.getWorldContainer(), worldName);
        try {
            FileUtils.copyDirectory(map.getMapDirectory(), worldDir);
        } catch (IOException e) {
            throw new MapLoadException("Failed to duplicate " + map.getMapDirectory().getPath(), e);
        }

        World world = WorldCreator.name(worldDir.getName()).createWorld();

        // Create and load map config
        WorldBlurSession newSession = getSession().addChildSession(new WorldBlurSession(getSession(), world));
        sessions.put(newSession, map);
        newSession.setName(map.getId());
        newSession.getModuleManager().getModuleLoader().load(map.getConfig().getModules());
        return newSession;
    }

    public BlurMap nextMap() {
        List<File> paths = getMapPaths();

        File file;
        if (isRandom()) {
            file = CollectionUtils.getRandomElement(paths);
        } else {
            int index = lastIndex > this.mapPaths.size() - 1 ? lastIndex = 0 : lastIndex++;
            file = paths.get(index);
        }
        BlurMap map = new BlurMap(this, file);
        ChooseNextMapEvent event = getSession().callEvent(new ChooseNextMapEvent(this, map));
        return event.getNextMap();
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public boolean isRandom() {
        return random;
    }

    public List<File> getMapPaths() {
        return Collections.unmodifiableList(mapPaths);
    }

    public List<BlurMap> getBlurMaps() {
        return getMapPaths().stream().map(file -> new BlurMap(this, file)).collect(Collectors.toList());
    }

    public Map<WorldBlurSession, BlurMap> getSessions() {
        return Collections.unmodifiableMap(this.sessions);
    }

    @Nullable
    public BlurMap getBlurMap(@Nonnull WorldBlurSession session) {
        Preconditions.checkNotNull(session, "session");
        return this.sessions.get(session);
    }

    public static final class MapLoaderData implements ModuleData {

        private final Set<File> mapPaths = new LinkedHashSet<>();
        private File directory;
        private Archive archive;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            Preconditions.checkNotNull(serialized.getAsObject(), "MapLoader data cannot be null.");

            Map<String, ?> map = serialized.getAsMap();
            Object directoryString = map.get("directory");
            Object rawMaps = checkNotNull(map.get("maps"), "MapLoader missing maps property.");

            if (!(directoryString instanceof String)) {
                this.directory = new File(".");
            } else {
                this.directory = new File(directoryString.toString());
                check(directory.exists(), "%s does not exist.", directory);
                check(directory.isDirectory(), "%s must be a directory.", directory);
            }

            // Direct list of map (directory) names
            if (rawMaps instanceof String) { // Single map in the form of a string
                addStrings(moduleManager.getLogger(), Stream.of((String) rawMaps));
            } else if (rawMaps instanceof List) {
                addStrings(moduleManager.getLogger(), ((List<String>) rawMaps).stream());
            } else if (rawMaps instanceof Map) {
                // map of key 'file' with the path to the file that contains maps.
                Map<String, Object> _map = (Map<String, Object>) rawMaps;
                String fileString = checkNotNull(_map.get("file"), "MapLoader maps missing file property.").toString();
                fileString = fileString.replaceAll("\\{directory\\}", this.directory.toString());

                File file = new File(directory, fileString);
                check(file.exists(), "%s does not exist.", file);
                check(file.isFile(), "%s must be a file.", file);

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    addStrings(moduleManager.getLogger(), br.lines());
                } catch (IOException e) {
                    throw new ModuleParseException(e.getMessage());
                }
            }
            Object random = map.get("random");
            boolean randomBool = random == null ? false : Boolean.valueOf(random.toString());

            if (map.containsKey("archive")) {
                Object o = map.get("archive");
                this.archive = null; // If archive is present, assume we must start archives, unless value is "false". See if-string.

                if (o instanceof String) {
                    // Check for true and false to toggle archiving.
                    if (o.toString().equalsIgnoreCase("true") || o.toString().equalsIgnoreCase("false")) {
                        // Only if the value is true do we disable archiving.
                        if (Boolean.parseBoolean(o.toString())) {
                            this.archive = new Archive();
                        }
                    } else {
                        this.archive = new Archive();
                        this.archive.directory = o.toString();
                    }
                } else if (o instanceof Map) { // Automatically load from class fields
                    this.archive = new Archive();
                    moduleManager.getModuleLoader().deserializeTo((Map) o, archive);

                    // Type-safe compression type.
                    String compress = this.archive.compress;
                    if (compress == null) {
                        this.archive.compressionType = ArchiveCompressionType.NONE;
                    } else {
                        // Boolean true = ZIP, false = NONE
                        if (compress.equalsIgnoreCase("true") || compress.equalsIgnoreCase("false")) {
                            this.archive.compressionType = Boolean.valueOf(compress) ? ArchiveCompressionType.ZIP : ArchiveCompressionType.NONE;
                        } else {
                            try {
                                this.archive.compressionType = ArchiveCompressionType.valueOf(compress.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                check(false, "%s is not a valid compression type. Compression types: %s", compress,
                                    Joiner.on(',').join(ArchiveCompressionType.values()));
                            }
                        }
                    }
                }
            }

            return new MapLoaderModule(moduleManager, this.directory, this.mapPaths, randomBool, archive);
        }

        private void addStrings(Logger logger, Stream<String> paths) throws ModuleParseException {
            List<File> files = paths.map(line -> new File(this.directory, line)).collect(Collectors.toList());

            /* ================================
             * >> /Informative Data
             * ================================ */
            List<File> _inexistant = new ArrayList<>();
            List<File> _nonDirs = new ArrayList<>();
            List<File> _nonMC = new ArrayList<>();
            for (File file : files) {
                switch (addMapDir(file)) { // This line adds the file directory to this.mapPaths
                    case 1:
                        _inexistant.add(file);
                        break;
                    case 2:
                        _nonDirs.add(file);
                        break;
                    case 3:
                        _nonMC.add(file);
                }
            }
            if (_inexistant.size() > 0) {
                logger.severe("The following directories do not exist: " + Joiner.on(", ").join(_inexistant));
            }
            if (_nonDirs.size() > 0) {
                logger.severe("The following files must be directories: " + Joiner.on(", ").join(_nonDirs));
            }
            if (_nonMC.size() > 0) {
                logger.severe("The following directories are not Minecraft worlds: " + Joiner.on(", ").join(_nonMC));
            }
            /* ================================
             * >> /Informative Data
             * ================================ */
        }

        private int addMapDir(String path) throws ModuleParseException {
            return addMapDir(new File(this.directory, path));
        }

        private int addMapDir(File file) throws ModuleParseException {
            if (!file.exists()) {
                return 1;
            }
            if (!file.isDirectory()) {
                return 2;
            }
            if (!isMinecraftWorldDir(file)) {
                return 3;
            }
            mapPaths.add(file);
            return 0;
        }

        private boolean isMinecraftWorldDir(File file) {
            return new File(file, "level.dat").exists() && new File(file, "region").exists();
        }
    }

    protected static final class Archive {

        private String directory = "./archives";
        private String compress = "true";
        @Name("name-template")
        private String nameTemplate = "{mapname}-{datetime}";

        // compressionType set from parse method above, represents compressing value.
        private transient ArchiveCompressionType compressionType = ArchiveCompressionType.ZIP;

        public String getDirectory() {
            return directory;
        }

        public String getCompress() {
            return compress;
        }

        public String getNameTemplate() {
            return nameTemplate;
        }

        public ArchiveCompressionType getCompressionTypeEnum() {
            return compressionType;
        }
    }
}
