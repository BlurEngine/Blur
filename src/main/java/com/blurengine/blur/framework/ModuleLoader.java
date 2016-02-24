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

package com.blurengine.blur.framework;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import com.blurengine.blur.BlurPlugin;
import com.blurengine.blur.framework.serializer.ModuleNotFoundException;
import com.blurengine.blur.framework.serializer.ModuleSerializer;
import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializer;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.serializer.FilterSerializer;
import com.blurengine.blur.modules.spawns.Spawn;
import com.blurengine.blur.modules.spawns.serializer.SpawnSerializer;
import com.blurengine.blur.modules.teams.BlurTeam;
import com.blurengine.blur.modules.teams.TeamSerializer;
import com.supaham.commons.bukkit.utils.SerializationUtils;
import com.supaham.commons.utils.ThrowableUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import pluginbase.config.serializers.Serializer;
import pluginbase.config.serializers.SerializerSet;
import pluginbase.config.serializers.SerializerSet.Builder;
import pluginbase.messages.messaging.SendablePluginBaseException;

public class ModuleLoader {

    private static final Map<String, ModuleInfo> moduleInfos = new HashMap<>();
    private static final BiMap<ModuleInfo, Class<? extends Module>> moduleInfoClasses = HashBiMap.create();
    private static final Map<Class<? extends Module>, Class<? extends ModuleData>> dataClasses = new HashMap<>();
    private static final Map<Class<? extends ModuleData>, ModuleInfo> dataClassesToInfo = new HashMap<>();

    private final ModuleManager moduleManager;

    private final SerializerSet serializerSet;
    private final ModuleSerializer moduleSerializer;
    private final FilterSerializer filterSerializer;
    private final TeamSerializer teamSerializer;
    private final ExtentSerializer extentSerializer;
    private final SpawnSerializer spawnSerializer;

    /**
     * Registers a {@link Module} class to this manager. This method puts the {@code moduleClass} as well as it's {@link ModuleInfo} in
     * {@link ModuleLoader#getDataClasses()}.
     *
     * @param moduleClass module class to register
     *
     * @return whether or not the method registered the class, the only case where it would return false is if the class is already registered
     */
    public static boolean register(@Nonnull Class<? extends Module> moduleClass) {
        Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null.");
        if (dataClasses.containsKey(moduleClass)) {
            return false;
        }

        if (moduleClass.isAnnotationPresent(Deprecated.class)) {
            BlurPlugin.get().getLog().warning("Registering deprecated class to ModuleLoader: %s", moduleClass.getName());
        }

        ModuleInfo annotation = moduleClass.getDeclaredAnnotation(ModuleInfo.class);
        Preconditions.checkNotNull(annotation, moduleClass.getName() + " must be annotated with @ModuleInfo.");
        Class<? extends ModuleData> dataClass = annotation.dataClass();
        Preconditions.checkNotNull(dataClass, moduleClass.getName() + " has a null data class value in @ModuleInfo.");

        moduleInfos.put(annotation.name().toLowerCase(), annotation);
        moduleInfoClasses.put(annotation, moduleClass);
        // annotations with ModuleData.class are default and do not need to be registered
        if (!annotation.dataClass().equals(ModuleData.class)) {
            dataClasses.put(moduleClass, dataClass);
            dataClassesToInfo.put(dataClass, annotation);
        }
        return true;
    }

    public static Collection<ModuleInfo> getModuleInfos() {
        return Collections.unmodifiableCollection(moduleInfos.values());
    }

    public static ModuleInfo getModuleInfoByModule(Class clazz) {
        return moduleInfoClasses.inverse().get(clazz);
    }

    public static ModuleInfo getModuleInfoByName(String name) {
        return moduleInfos.get(name.toLowerCase());
    }

    public static Map<Class<? extends Module>, Class<? extends ModuleData>> getDataClasses() {
        return Collections.unmodifiableMap(dataClasses);
    }

    public ModuleLoader(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        Builder builder = SerializerSet.builder(SerializationUtils.SERIALIZER_SET);
        __add(builder, Module.class, moduleSerializer = new ModuleSerializer(this));
        __add(builder, Filter.class, filterSerializer = new FilterSerializer(this));
        _add(builder, BlurTeam.class, teamSerializer = new TeamSerializer(this));
        __add(builder, Extent.class, extentSerializer = new ExtentSerializer(this));
        __add(builder, Spawn.class, spawnSerializer = new SpawnSerializer(this));
        serializerSet = builder.build();
    }

    // Adds to builder, with the class being a common superclass, e.g. getting the Extent serializer when given CuboidExtent.   
    private static void __add(Builder builder, Class clazz, Serializer serializer) {
        _add(builder, clazz, serializer);
        builder.registerClassReplacement(clazz::isAssignableFrom, clazz);
    }

    private static void _add(Builder builder, Class clazz, Serializer serializer) {
        builder.addSerializer(clazz, () -> serializer);
    }

    public void load(List<Object> list) {
        for (Object o : list) {
            try {
                moduleSerializer.deserialize(o, null, getSerializerSet());
            } catch (ModuleNotFoundException e) {
                Collection<String> similarModuleNames = ModuleNameHelper.getSimilarModuleNames(e.getName());
                String message;
                if (similarModuleNames.isEmpty()) {
                    message = String.format("Unknown module '%s'.", e.getName());
                } else {
                    message = String.format("Unknown module '%s'. Did you mean: %s", e.getName(), Joiner.on(", ").join(similarModuleNames));
                }

                if (this.moduleManager.getLogger().getDebugLevel() == 0) {
                    this.moduleManager.getLogger().severe(message);
                } else {
                    this.moduleManager.getLogger().log(Level.SEVERE, message, e);
                }
            }
        }
    }

    /**
     * Creates a {@link Module} instance from a {@link ModuleData} class. This assumes that a Module class has been registered using {@link
     * #register(Class)}, which is annotated with {@link ModuleInfo}.
     *
     * @param data data to use to create module
     *
     * @return
     */
    public Module createModule(@Nonnull ModuleInfo moduleInfo, Object data) {
        Preconditions.checkNotNull(moduleInfo, "moduleInfo cannot be null.");
        // No Data class, just look for a constructor that takes ModuleManager param.
        if (moduleInfo.dataClass().equals(ModuleData.class)) {
            Class<? extends Module> moduleClass = moduleInfoClasses.get(moduleInfo);
            try {
                Constructor<? extends Module> ctor = moduleClass.getDeclaredConstructor(ModuleManager.class);
                boolean accessible = ctor.isAccessible();
                if (!accessible) {
                    ctor.setAccessible(true);
                }
                Module module = ctor.newInstance(this.moduleManager);
                if (!accessible) {
                    ctor.setAccessible(false);
                }
                return moduleManager.addModule(module);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Throwable t = e;
                if (e.getCause() != null) {
                    t = e.getCause();
                }
                t.printStackTrace();
                return null;
            }
        }

        try {
            Constructor<? extends ModuleData> ctor = moduleInfo.dataClass().getDeclaredConstructor();
            boolean accessible = ctor.isAccessible();
            if (!accessible) {
                ctor.setAccessible(true);
            }
            ModuleData moduleData = ctor.newInstance();
            if (!accessible) {
                ctor.setAccessible(false);
            }
            return createModule(moduleInfo, moduleData, data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a {@link Module} instance out of a {@link ModuleData}.
     *
     * @param data data to use to create module
     *
     * @return
     */
    public Module createModule(@Nonnull ModuleInfo moduleInfo, @Nonnull ModuleData moduleData, Object data) {
        Preconditions.checkNotNull(moduleInfo, "moduleInfo cannot be null.");
        Preconditions.checkNotNull(moduleData, "moduleData cannot be null.");
        try {
            return this.moduleManager.addModule(moduleData.parse(this.moduleManager, new SerializedModule(this, data)));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("deserializing")) { // PluginBase wrapped RuntimeException
                e = (Exception) ThrowableUtils.getCause(e);
            }
            // exception must have a message in order to not go all spammy with stacktraces.
            if (e.getMessage() != null && this.moduleManager.getLogger().getDebugLevel() == 0) {
                this.moduleManager.getLogger().severe("Error loading Module %s: %s", moduleInfo.name(), e.getMessage());
            } else {
                this.moduleManager.getLogger().log(Level.SEVERE, "Error whilst loading Module " + moduleInfo.name(), e);
            }
            return null;
        }
    }

    /* ================================
     * >> UTILITY METHODS
     * ================================ */

    public Module createModuleQuickParse(ModuleData moduleData, Object data) {
        return createModule(this.dataClassesToInfo.get(moduleData.getClass()), moduleData, data);
    }

    public <T extends ModuleData> T deserializeTo(Map map, @Nonnull T moduleData) {
        return SerializationUtils.loadToObject(map, Preconditions.checkNotNull(moduleData, "moduleData cannot be null."), serializerSet);
    }

    public <T> T deserializeTo(Map map, @Nonnull T destination) {
        return SerializationUtils.loadToObject(map, Preconditions.checkNotNull(destination, "destination cannot be null."), serializerSet);
    }

    public boolean deserializeYAMLFileTo(File file, Object destination) {
        try {
            SerializationUtils.yaml(file).setAlternateSerializerSet(serializerSet).build().loadToObject(destination);
            return true;
        } catch (SendablePluginBaseException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /* ================================
     * >> GETTERS & SETTERS
     * ================================ */

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public SerializerSet getSerializerSet() {
        return serializerSet;
    }

    public ModuleSerializer getModuleSerializer() {
        return moduleSerializer;
    }

    public FilterSerializer getFilterSerializer() {
        return filterSerializer;
    }

    public ExtentSerializer getExtentSerializer() {
        return extentSerializer;
    }

    public TeamSerializer getTeamSerializer() {
        return teamSerializer;
    }

    public SpawnSerializer getSpawnSerializer() {
        return spawnSerializer;
    }
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public Logger getLogger() {
        return moduleManager.getLogger();
    }
}
