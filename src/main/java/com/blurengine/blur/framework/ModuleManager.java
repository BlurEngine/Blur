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

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.blurengine.blur.framework.ticking.TickFieldHolder;
import com.blurengine.blur.modules.extents.ExtentManager;
import com.blurengine.blur.modules.filters.FilterManager;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.modules.teams.TeamManager;
import com.blurengine.blur.session.BlurSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final Map<Class, Boolean> INTERNAL_MODULES = new HashMap<>();

    @Nullable private final ModuleManager parentManager;
    private final ModuleLoader moduleLoader;
    private final BlurSession session;
    private final ListMultimap<Class<? extends Module>, Module> modules = LinkedListMultimap.create();

    private TickFieldHolder tickFieldHolder;
    private FilterManager filterManager;
    private ExtentManager extentManager;
    private TeamManager teamManager;
    private StageManager stageManager;

    public static boolean isInternalModule(Module module) {
        // CACHE
        Boolean aBoolean = INTERNAL_MODULES.get(module.getClass());
        if (aBoolean != null) {
            return aBoolean;
        }

        // GENERATE CACHE
        boolean found = false;
        Class clazz = module.getClass();
        do {
            if (clazz.isAnnotationPresent(InternalModule.class)) {
                found = true;
                break;
            }
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));
        INTERNAL_MODULES.put(module.getClass(), found);
        return found;
    }

    public ModuleManager(@Nonnull BlurSession session) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = null;
        init();
        this.moduleLoader = new ModuleLoader(this);
    }

    public ModuleManager(@Nonnull BlurSession session, @Nonnull ModuleLoader moduleLoader) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = null;
        init();
        this.moduleLoader = Preconditions.checkNotNull(moduleLoader, "moduleLoader cannot be null.");
    }

    public ModuleManager(@Nonnull BlurSession session, @Nonnull ModuleManager parentManager) {
        this.session = Preconditions.checkNotNull(session, "session cannot be null.");
        this.parentManager = Preconditions.checkNotNull(parentManager, "parentManager cannot be null.");
        init();
        this.moduleLoader = parentManager.moduleLoader;
    }

    private void init() {
        // The following set of modules don't use addModule as actual modules depend on them.
        this.modules.put(TickFieldHolder.class, this.tickFieldHolder = new TickFieldHolder(this));

        addModule(filterManager = new FilterManager(this));
        addModule(extentManager = new ExtentManager(this));
        addModule(teamManager = new TeamManager(this));
        addModule(stageManager = new StageManager(this));
    }

    protected Module addModule(Module module) {
        Preconditions.checkNotNull(module, "module cannot be null.");
        this.modules.put(module.getClass(), module);
        return module;
    }

    public void load() {
        // Add in the default stage if none have been defined.
        if (this.stageManager.getStages().isEmpty()) {
            this.stageManager.addDefaultStage();
        }
        this.modules.values().stream().filter(module -> module.getState() == ComponentState.UNLOADED).forEach(this::loadModule);
    }

    public void unload() {
        this.modules.values().stream().filter(module -> module.getState() == ComponentState.LOADED).forEach(this::unloadModule);
    }

    public void enable() {
        this.modules.values().stream().filter(module -> module.getState() == ComponentState.LOADED).forEach(this::enableModule);
    }

    public void disable() {
        this.modules.values().stream().filter(module -> module.getState() == ComponentState.ENABLED).forEach(this::disableModule);
    }

    public boolean loadModule(Module module) {
        if (module.getState().isLoaded()) {
            return false;
        }
        String name = module.getClass().getName();
        try {
            name = checkName(module);
            if (!isInternalModule(module)) {
                getLogger().fine("Loading module %s", name);
            }
            module.tryLoad();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error loading Module %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error loading Module " + name, e);
            }
        }
        return false;
    }

    public boolean unloadModule(Module module) {
        if (!module.getState().isLoaded()) {
            return false;
        }
        String name = module.getClass().getName();
        try {
            name = checkName(module);
            if (!isInternalModule(module)) {
                getLogger().fine("Unloading module %s", name);
            }
            module.tryUnload();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error unloading Module %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error unloading Module " + name, e);
            }
        }
        return false;
    }

    public boolean enableModule(Module module) {
        if (module.getState() == ComponentState.ENABLED) {
            return false;
        }
        String name = module.getClass().getName();
        try {
            name = checkName(module);
            if (!isInternalModule(module)) {
                getLogger().fine("Enabling module %s", name);
            }
            module.tryEnable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error enabling Module %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error enabling Module " + name, e);
            }
        }
        return false;
    }

    public boolean disableModule(Module module) {
        // != ENABLED means either unloaded or loaded (which are both disabled state)
        if (module.getState() != ComponentState.ENABLED) {
            return false;
        }

        String name = module.getClass().getName();
        try {
            name = checkName(module);
            if (!isInternalModule(module)) {
                getLogger().fine("Disabling module %s", name);
            }
            module.tryDisable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error disabling Module %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error disabling Module " + name, e);
            }
        }
        return false;
    }

    // TODO Add boolean for skipping internal module check?
    private String checkName(Module module) {
        if (module.getModuleInfo() == null) {
            if (isInternalModule(module)) {
                return module.getClass().getSimpleName();
            }
            throw new IllegalStateException(module.getClass().getName() + " must be annotated with @ModuleInfo and registered to ModuleLoader.");
        }
        return module.getModuleInfo().name();
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
    @Nonnull
    public <T extends Module> List<T> getModule(@Nonnull Class<T> clazz) {
        //noinspection unchecked
        return (List<T>) this.modules.get(Preconditions.checkNotNull(clazz, "clazz cannot be null."));
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

    public TickFieldHolder getTickFieldHolder() {
        return tickFieldHolder;
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
