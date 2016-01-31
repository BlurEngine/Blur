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

package com.blurengine.blur;

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.BoundariesModule;
import com.blurengine.blur.modules.DummyModule;
import com.blurengine.blur.modules.InvulnerableModule;
import com.blurengine.blur.modules.MapInfoModule;
import com.blurengine.blur.modules.extents.ExtentManager;
import com.blurengine.blur.modules.filters.FilterManager;
import com.blurengine.blur.modules.framework.ModuleLoader;
import com.blurengine.blur.modules.framework.ticking.TickFieldHolder;
import com.blurengine.blur.modules.goal.GoalModule;
import com.blurengine.blur.modules.goal.LastPlayerAliveWinnerModule;
import com.blurengine.blur.modules.goal.LastTeamAliveWinnerModule;
import com.blurengine.blur.modules.includes.IncludesModule;
import com.blurengine.blur.modules.maploading.MapLoaderModule;
import com.blurengine.blur.modules.spawns.SpawnsModule;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.modules.teams.TeamManager;
import com.blurengine.blur.session.SessionManager;
import com.supaham.commons.bukkit.CommonPlugin;
import com.supaham.commons.bukkit.modules.ModuleContainer;

import javax.annotation.Nonnull;

import pluginbase.logging.PluginLogger;

/**
 * Blur main class.
 */
public class Blur {

    private final CommonPlugin plugin;
    private final ModuleContainer moduleContainer;
    private final SessionManager sessionManager;
    private final PluginLogger logger;

    public Blur(@Nonnull CommonPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be null.");
        this.moduleContainer = new ModuleContainer(plugin.getModuleContainer());
        this.sessionManager = new SessionManager(this);
        this.logger = plugin.getLog();
    }

    static {
        /* ================================
         * >> CORE
         * ================================ */
        ModuleLoader.register(TickFieldHolder.class);
        ModuleLoader.register(SpawnsModule.class);
        ModuleLoader.register(FilterManager.class);
        ModuleLoader.register(ExtentManager.class);
        ModuleLoader.register(TeamManager.class);
        ModuleLoader.register(StageManager.class);

        /* ================================
         * >> OPTIONAL
         * ================================ */
        ModuleLoader.register(MapLoaderModule.class);
        ModuleLoader.register(MapInfoModule.class);
        ModuleLoader.register(IncludesModule.class);
        ModuleLoader.register(DummyModule.class);
        ModuleLoader.register(BoundariesModule.class);
        ModuleLoader.register(InvulnerableModule.class);
        ModuleLoader.register(LastPlayerAliveWinnerModule.class);
        ModuleLoader.register(LastTeamAliveWinnerModule.class);
        ModuleLoader.register(GoalModule.class);
    }

    public CommonPlugin getPlugin() {
        return plugin;
    }

    public ModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public PluginLogger getLogger() {
        return logger;
    }
}
