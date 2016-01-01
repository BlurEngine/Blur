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
import com.supaham.commons.bukkit.utils.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import pluginbase.config.datasource.yaml.YamlDataSource;

/**
 * Bukkit plugin class for {@link Blur}.
 */
@Getter
public class BlurPlugin extends SimpleCommonPlugin<BlurPlugin> {

    private static final String COMMAND_PREFIX = "b";
    private static BlurPlugin instance;

    private BlurSettings settings = new BlurSettings();
    private Blur blur;
    private RootBlurSession rootSession;

    public static BlurPlugin get() { return instance; }

    public BlurPlugin() {
        super(BlurPlugin.class, COMMAND_PREFIX);
        Preconditions.checkState(instance == null, "BlurPlugin already initialized.");
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.blur = new Blur(this);
        this.rootSession = new RootBlurSession(this.blur.getSessionManager());
        YamlDataSource yaml;
        try {
            yaml = SerializationUtils.yaml(new File(getDataFolder(), "blur.yml")).build();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        SerializationUtils.loadOrCreateProperties(getLog(), yaml, this.settings);
        ModuleManager moduleManager = this.rootSession.getModuleManager();

        // Load from serialized modules, here's how the whoooole chain starts!
        moduleManager.getModuleLoader().load(getSettings().getModules());

        // Immediately load, enable and start the root session to get the wheels going.
        new TickerTask(this, 1, this.rootSession::start).start();
        new TickerTask(this, 200, this.rootSession::stop).start();

        enableMetrics();
    }


    private AtomicInteger tries = new AtomicInteger();

    private void enableMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
            if (tries.incrementAndGet() <= 3) {
                new TickerTask(this, 600 * tries.get(), this::enableMetrics);
            }
        }
    }
}
