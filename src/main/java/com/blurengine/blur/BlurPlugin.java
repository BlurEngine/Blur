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

import com.blurengine.blur.commands.BlurCommandProviders;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.modules.stages.StageChangeData;
import com.blurengine.blur.modules.stages.StageChangeReasons;
import com.blurengine.blur.session.RootBlurSession;
import com.blurengine.blur.supervisor.BlurReportContext;
import com.supaham.commons.bukkit.ServerShutdown;
import com.supaham.commons.bukkit.ServerShutdown.ServerShutdownEvent;
import com.supaham.commons.bukkit.SimpleCommonPlugin;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.listeners.PlayerListeners;
import com.supaham.commons.state.State;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import co.aikar.commands.BukkitCommandIssuer;

/**
 * Bukkit plugin class for {@link Blur}.
 */
public class BlurPlugin extends SimpleCommonPlugin<BlurPlugin> implements Listener {

    /*
     * Counter for how many plugins hooked in here for waiting before loading we start the root session.
     */
    private static int pluginsHooked = 0;

    private static BlurPlugin instance;

    private Blur blur;
    private RootBlurSession rootSession;

    public static BlurPlugin get() { return instance; }

    /**
     * Registers a plugin that is hooking into Blur and requires priority of loading before starting RootBlurSession.
     * @param plugin
     */
    public static void hook(Plugin plugin) {
        pluginsHooked++;
    }

    /**
     * Reverses pause effect from {@link #hook(Plugin)}.
     * @param plugin
     */
    public static void unhook(Plugin plugin) {
        if (pluginsHooked > 0) {
            pluginsHooked--;
            if (instance != null) {
                instance.startRootSession();
            }
        }
    }

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

        // This is important for handling immediate shutdown via commands.
        ServerShutdown module = new ServerShutdown(getModuleContainer());
        getModuleContainer().register(module);
        module.setState(State.ACTIVE);
        PlayerListeners.defaultSpeeds(this);
        registerEvents(this);
        registerEvents(new BlurListener(this));

        setupCommands();

        // SUPERVISOR
        if (getServer().getPluginManager().getPlugin("Supervisor") != null) {
            BlurReportContext.load(this);
        }

        if (!enableMetrics()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (pluginsHooked == 0) {
            startRootSession();
        } else {
            // Immediately load, enable and start the root session to get the wheels going.
            new TickerTask(this, 0, this::startRootSession).start();
        }
    }

    private void startRootSession() {
        if (this.rootSession.isStarted()) {
            // Mission accomplished
            return;
        }
        // Clear hook count
        pluginsHooked = 0;

        ModuleManager moduleManager = this.rootSession.getModuleManager();
        // Load from serialized modules, here's how the whoooole chain starts!
        moduleManager.getModuleLoader().load(getSettings().getModules());
        this.rootSession.start();
    }

    // Cleanup blur during shutdown.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerShutdown(ServerShutdownEvent event) {
        cleanup();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        cleanup();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BukkitCommandIssuer issuer = getCommandsManager().getCommandIssuer(sender);
        getCommandsManager().getRootCommand(label).execute(issuer, label, args);
        return super.onCommand(sender, command, label, args);
    }

    private void cleanup() {
        if (this.rootSession != null) {
            StageChangeData stopData = new StageChangeData(StageChangeReasons.SHUTDOWN);
            this.rootSession.stop(stopData);
        }
    }

    private void setupCommands() {
        new BlurCommandProviders(this).registerAll(getCommandsManager().getCommandContexts());
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
