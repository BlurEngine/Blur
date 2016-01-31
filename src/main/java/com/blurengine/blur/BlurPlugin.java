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

import com.blurengine.blur.modules.framework.ModuleManager;
import com.supaham.commons.bukkit.SimpleCommonPlugin;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.commands.common.CommonCommands;
import com.supaham.commons.bukkit.utils.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import pluginbase.config.datasource.yaml.YamlDataSource;

/**
 * Bukkit plugin class for {@link Blur}.
 */
public class BlurPlugin extends SimpleCommonPlugin<BlurPlugin> {

    private static BlurPlugin instance;

    private Blur blur;
    private RootBlurSession rootSession;

    public static BlurPlugin get() { return instance; }

    public BlurPlugin() {
        Preconditions.checkState(instance == null, "BlurPlugin already initialized.");
        instance = this;
        setSettings(() -> new BlurSettings(this));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.blur = new Blur(this);
        this.rootSession = new RootBlurSession(this.blur.getSessionManager());
        ModuleManager moduleManager = this.rootSession.getModuleManager();

        // Load from serialized modules, here's how the whoooole chain starts!
        moduleManager.getModuleLoader().load(getSettings().getModules());

        if (!enableMetrics()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        CommonCommands.DEBUG.builder(this, "b").register();

        // Immediately load, enable and start the root session to get the wheels going.
        new TickerTask(this, 1, this.rootSession::start).start();
        new TickerTask(this, 200, this.rootSession::stop).start();

        new TickerTask(this, 0, getCommandsManager()::build).start();
    }


    private AtomicInteger tries = new AtomicInteger();

    public BlurSettings getSettings() {
        return (BlurSettings) super.getSettings();
    }

    public Blur getBlur() {
        return blur;
    }

    public RootBlurSession getRootSession() {
        return rootSession;
    }
}
