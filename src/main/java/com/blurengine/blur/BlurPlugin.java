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
import com.supaham.commons.bukkit.ServerShutdown;
import com.supaham.commons.bukkit.ServerShutdown.ServerShutdownEvent;
import com.supaham.commons.bukkit.SimpleCommonPlugin;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.commands.common.CommonCommands;
import com.supaham.commons.state.State;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Bukkit plugin class for {@link Blur}.
 */
public class BlurPlugin extends SimpleCommonPlugin<BlurPlugin> implements Listener {

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

        // This is important for handling immediate shutdown via commands.
        ServerShutdown module = new ServerShutdown(getModuleContainer());
        getModuleContainer().register(module);
        module.setState(State.ACTIVE);
        registerEvents(this);

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

    // Cleanup blur during shutdown.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerShutdown(ServerShutdownEvent event) {
        if (this.rootSession != null) {
            this.rootSession.stop();
        }
    }

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
