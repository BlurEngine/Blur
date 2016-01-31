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

package com.blurengine.blur.modules.framework;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.blurengine.blur.modules.extents.ExtentManager;
import com.blurengine.blur.modules.filters.FilterManager;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.modules.teams.TeamManager;
import com.blurengine.blur.session.BlurSession;

import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.logging.PluginLogger;

/**
 * This is a {@link Module} manager, used to handle Module instance creation, etc.
 *
 * @see Module
 */
public class ModuleManager {

    @Nullable private final ModuleManager parentManager;
    private final ModuleLoader moduleLoader;
    private final BlurSession session;
    private final Multimap<Class<? extends Module>, Module> modules;

    private final FilterManager filterManager;
    private final ExtentManager extentManager;
    private final TeamManager teamManager;
    private final StageManager stageManager;

    {
        modules = HashMultimap.create();
        addModule(filterManager = new FilterManager(this));
        addModule(extentManager = new ExtentManager(this));
        addModule(teamManager = new TeamManager(this));
        addModule(stageManager = new StageManager(this));
    }

    public ModuleManager(@Nonnull BlurSession session) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = null;
        this.moduleLoader = new ModuleLoader(this);
    }

    public ModuleManager(@Nonnull BlurSession session, @Nonnull ModuleLoader moduleLoader) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = null;
        this.moduleLoader = Preconditions.checkNotNull(moduleLoader, "moduleLoader cannot be null.");
    }

    public ModuleManager(@Nonnull BlurSession session, @Nonnull ModuleManager parentManager) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = Preconditions.checkNotNull(parentManager, "parentManager cannot be null.");
        this.moduleLoader = parentManager.moduleLoader;
    }

    protected Module addModule(Module module) {
        if (module != null) {
            this.modules.put(module.getClass(), module);
        }
        return module;
    }

    public void load() {
        // Add in the default stage if none have been defined.
        if (this.stageManager.getStages().isEmpty()) {
            this.stageManager.addDefaultStage();
        }

        this.modules.values().stream().filter(module -> module.getState() == ModuleState.UNLOADED).forEach(this::loadModule);
    }

    public void unload() {
        this.modules.values().stream().filter(module -> module.getState() == ModuleState.LOADED).forEach(this::unloadModule);
    }

    public void enable() {
        this.modules.values().stream().filter(module -> module.getState() == ModuleState.LOADED).forEach(this::enableModule);
    }

    public void disable() {
        this.modules.values().stream().filter(module -> module.getState() == ModuleState.ENABLED).forEach(this::disableModule);
    }

    public boolean loadModule(Module module) {
        getLogger().fine("Loading module %s", module.getModuleInfo().name());
        try {
            module.load();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error loading Module %s: %s", module.getModuleInfo().name(), e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error loading Module " + module.getModuleInfo().name(), e);
            }
        }
        return false;
    }

    public boolean unloadModule(Module module) {
        getLogger().fine("Unloading module %s", module.getModuleInfo().name());
        try {
            module.unload();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error unloading Module %s: %s", module.getModuleInfo().name(), e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error unloading Module " + module.getModuleInfo().name(), e);
            }
        }
        return false;
    }

    public boolean enableModule(Module module) {
        getLogger().fine("Enabling module %s", module.getModuleInfo().name());
        try {
            module.enable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error enabling Module %s: %s", module.getModuleInfo().name(), e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error enabling Module " + module.getModuleInfo().name(), e);
            }
        }
        return false;
    }

    public boolean disableModule(Module module) {
        getLogger().fine("Disabling module %s", module.getModuleInfo().name());
        try {
            module.disable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error disabling Module %s: %s", module.getModuleInfo().name(), e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error disabling Module " + module.getModuleInfo().name(), e);
            }
        }
        return false;
    }

    /**
     * Unregisters a {@link Module} from this container. If the given module was never registered to this module, false is always returned.
     *
     * @param module module to destroy
     *
     * @return whether the removed module is equal to the given module
     */
    public boolean destroyModule(@Nonnull Module module) {
        Preconditions.checkNotNull(module, "module cannot be null.");
        return this.modules.remove(module.getClass(), module);
    }

    /**
     * Gets a {@link Module} by {@link Class}. The returned module is null only if this plugin does not register the class.
     *
     * @param clazz class of the module to get
     * @param <T> type of module
     *
     * @return module instance, nullable
     */
    @Nullable
    public <T extends Module> T getModule(@Nonnull Class<T> clazz) {
        //noinspection unchecked
        return (T) this.modules.get(Preconditions.checkNotNull(clazz, "clazz cannot be null."));
    }

    /**
     * Returns an unmodifiable map of all the registered module classes and their instances in this container.
     *
     * @return a map of classes and their module instances
     */
    public Multimap<Class<? extends Module>, Module> getModules() {
        return Multimaps.unmodifiableMultimap(this.modules);
    }

    @Nullable
    public ModuleManager getParentManager() {
        return parentManager;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public BlurSession getSession() {
        return session;
    }

    public FilterManager getFilterManager() {
        return filterManager;
    }

    public ExtentManager getExtentManager() {
        return extentManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public StageManager getStageManager() {
        return stageManager;
    }
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public PluginLogger getLogger() {
        return this.session.getLogger();
    }
}