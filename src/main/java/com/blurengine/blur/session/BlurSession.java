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

package com.blurengine.blur.session;

import com.google.common.base.Preconditions;

import com.blurengine.blur.Blur;
import com.blurengine.blur.RootBlurSession;
import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent;
import com.blurengine.blur.events.players.PlayerPostLeaveSessionEvent;
import com.blurengine.blur.events.players.PlayerSwitchSessionEvent;
import com.blurengine.blur.events.session.SessionEnableEvent;
import com.blurengine.blur.events.session.SessionLoadEvent;
import com.blurengine.blur.events.session.SessionPreLoadEvent;
import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.events.session.SessionStopEvent;
import com.blurengine.blur.framework.Component;
import com.blurengine.blur.framework.ComponentState;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.SharedComponent;
import com.blurengine.blur.framework.metadata.BasicMetadataStorage;
import com.blurengine.blur.framework.metadata.MetadataStorage;
import com.blurengine.blur.framework.metadata.playerdata.PlayerData;
import com.supaham.commons.CommonCollectors;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.scoreboards.CommonScoreboard;
import com.supaham.commons.bukkit.utils.ChatUtils;
import com.supaham.commons.bukkit.utils.EventUtils;
import com.supaham.commons.utils.StringUtils;

import net.kyori.text.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.logging.PluginLogger;

/**
 * Represents a {@link Blur} session class that contains everything useful for modules to function. A BlurSession includes the following
 * <ul>
 *     <li>a singleton {@link SessionManager}. This isn't useful for the average gameplay module.</li>
 *     <li>A {@link ModuleManager} instance. This ModuleManager contains a list of {@link Module}s shared across child BlurSessions. The ModuleManager 
 *     could either: 
 *         <ol>
 *             <li>belong to the parent BlurSession.</li>
 *             <li>Be a new instance as a result of this BlurSession being the first session after the {@link RootBlurSession}.</li>
 *         </ol>
 *     </li>
 *     <li>A parent BlurSession instance. This BlurSession is nullable, typically if it's the {@link RootBlurSession}. Children sessions were 
 *     introduced to allow for multiple {@link WorldBlurSession} to belong to the same session so to speak. Allowing multiple worlds to be played
 *     in one session.</li>
 *     <li>A {@link Set} of children BlurSession. Same as above, with the exception that a BlurSession is also aware of its children.</li>
 *     <li>A root {@link File} directory. This should be utilized when interacting with local files.</li>
 *     <li>A boolean named {@code paused}. Each BlurSession can be paused when needed, which halts gameplay.</li>
 *     <li>A Map of {@link UUID} and {@link BlurPlayer} which may be accessed through {@link #getPlayers()} alongside a few utility methods such as
 *     {@link #getPlayers(Predicate)} and {@link #getPlayersStream()}</li>
 * </ul> 
 */
public abstract class BlurSession {

    protected final SessionManager sessionManager;
    protected final ModuleManager moduleManager;
    private final BlurSession parentSession;
    private final BlurSessionListener listener = new BlurSessionListener(this);
    private final Set<BlurSession> childrenSessions = new HashSet<>();
    private File rootDirectory = new File(".");
    private String name = getClass().getSimpleName(); // Default session name to short class name
    private int ticksPerSecond = 20;

    private boolean started;
    private boolean paused;
    private Instant startedAt;
    private int playedTicks;
    private SessionTicker ticker;

    private final Map<UUID, BlurPlayer> players = new HashMap<>();
    //    private final Table<BlurPlayer, Class, Object> customData = HashBasedTable.create();
    private final MetadataStorage<BlurPlayer> playerMetadata = new BasicMetadataStorage<>();

    private net.kyori.text.Component messagePrefix = TextComponent.of("");

    private final List<Runnable> onStopTasks = new ArrayList<>();
    private ComponentState state = ComponentState.UNLOADED;

    private Map<Class<? extends SharedComponent>, SharedComponent> sharedComponents = new HashMap<>();

    {
        setTicksPerSecond(20);
    }

    protected BlurSession(@Nonnull BlurSession parentSession, @Nullable ModuleManager moduleManager) {
        Preconditions.checkNotNull(parentSession, "parentSession cannot be null.");
        this.sessionManager = parentSession.getSessionManager();
        this.moduleManager = moduleManager != null ? moduleManager : new ModuleManager(this);
        this.parentSession = parentSession;
    }

    protected BlurSession(@Nonnull SessionManager sessionManager, @Nullable ModuleManager moduleManager) {
        Preconditions.checkNotNull(sessionManager, "sessionManager cannot be null.");
        this.sessionManager = sessionManager;
        this.moduleManager = moduleManager != null ? moduleManager : new ModuleManager(this);
        this.parentSession = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=" + getName() + "}";
    }

    public <T extends BlurSession> T addChildSession(T session) {
        // Future precaution, this will never be the case as of the time of writing this as equals() has not been overridden.
        Preconditions.checkState(this.childrenSessions.add(session), "New session somehow already exists.");
        // Add this child session to the SessionManager to easily keep track of it.
        getSessionManager().addSession(session);
        return session;
    }

    public boolean removeChildSession(@Nonnull BlurSession session) {
        Preconditions.checkNotNull(session, "session cannot be null.");
        this.childrenSessions.remove(session);
        return getSessionManager().removeSession(session);
    }

    public boolean load() {
        if (!setState(ComponentState.LOADED)) {
            return false;
        }
        getLogger().fine("Loading %s", getName());
        callEvent(new SessionPreLoadEvent(this));
        Preconditions.checkArgument(getTicksPerSecond() > 0, "ticksPerSecond must be greater than 0.");
        long startedAt = System.currentTimeMillis();
        this.ticker = new SessionTicker();
        getBlur().getPlugin().registerEvents(this.listener);
        this.sharedComponents.values().stream()
            .filter(module -> module.getState() == ComponentState.UNLOADED)
            .forEach(this::loadSharedComponent);
        this.moduleManager.load();
        callEvent(new SessionLoadEvent(this));
        getLogger().fine("%s loaded in %dms", getName(), System.currentTimeMillis() - startedAt);
        return true;
    }

    public boolean enable() {
        if (!setState(ComponentState.ENABLED)) {
            return false;
        }
        getLogger().fine("Enabling %s", getName());
        long startedAt = System.currentTimeMillis();
        this.sharedComponents.values().stream()
            .filter(module -> module.getState() == ComponentState.LOADED)
            .forEach(this::loadSharedComponent);
        this.moduleManager.enable();
        // Delay event by a tick to give the server time to catch up if it took too long loading the session.
        new TickerTask(getBlur().getPlugin(), 1, () -> callEvent(new SessionEnableEvent(this))).start();
        getLogger().fine("%s enabled in %dms", getName(), System.currentTimeMillis() - startedAt);
        return true;
    }

    public boolean start() {
        if (this.started) {
            return false;
        }
        if (!this.state.isLoaded()) {
            load();
        }
        if (this.state != ComponentState.ENABLED) {
            enable();
        }
        getLogger().fine("Starting %s", getName());
        this.startedAt = Instant.now();
        long startedAt = this.startedAt.toEpochMilli();
        callEvent(new SessionStartEvent(this));
        this.ticker.start();
        this.started = true;
        getLogger().fine("%s started in %dms", getName(), System.currentTimeMillis() - startedAt);
        return true;
    }

    public void stop() {
        if (!started) {
            return;
        }
        long startedAt = System.currentTimeMillis();
        this.started = false;
        this.childrenSessions.forEach(BlurSession::stop);
        getLogger().fine("Stopping %s", getName());
        callEvent(new SessionStopEvent(this));

        this.moduleManager.disable();
        this.sharedComponents.values().stream()
            .filter(module -> module.getState() == ComponentState.ENABLED)
            .forEach(this::disableSharedComponent);

        this.moduleManager.unload();
        this.sharedComponents.values().stream()
            .filter(module -> module.getState() == ComponentState.LOADED)
            .forEach(this::unloadSharedComponent);

        for (BlurPlayer blurPlayer : new HashSet<>(this.players.values())) {
            removePlayer(blurPlayer, false);
        }
        this.ticker.stop();
        this.ticker = null;
        getBlur().getPlugin().unregisterEvents(this.listener);
        this.onStopTasks.forEach(Runnable::run);
        if (this.parentSession != null) {
            this.parentSession.removeChildSession(this);
        }
        getLogger().fine("%s stopped in %dms", getName(), System.currentTimeMillis() - startedAt);
    }

    public BlurPlayer getPlayer(@Nonnull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null.");
        return getPlayer(player.getUniqueId()).orElse(getBlur().getPlayer(player));
    }

    public Optional<BlurPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(this.players.get(uuid));
    }

    public void addPlayer(@Nonnull BlurPlayer blurPlayer) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (!this.players.containsKey(blurPlayer.getUuid())) {
            if (blurPlayer.blurSession != null && !(blurPlayer.blurSession instanceof RootBlurSession)) {
                PlayerSwitchSessionEvent switchEvent = callEvent(new PlayerSwitchSessionEvent(blurPlayer, this));
                if (switchEvent.isCancelled()) {
                    return;
                }
                if (switchEvent.getNextSession() != null && switchEvent.getNextSession() != this) {
                    switchEvent.getNextSession().addPlayer(blurPlayer);
                    return;
                }
            }
            getLogger().finer("Adding %s to %s", blurPlayer.getName(), getName());
            this.players.put(blurPlayer.getUuid(), blurPlayer);
            blurPlayer.blurSession = this;
            initializePlayerDataClasses(blurPlayer);
            callEvent(new PlayerJoinSessionEvent(blurPlayer, this, false));
        }
    }

    private void initializePlayerDataClasses(BlurPlayer blurPlayer) {
        getLogger().fine("Initializing player data classes for %s", blurPlayer.getName());
        for (Class<? extends Module> clazz : moduleManager.getModules().keySet()) {
            Module module = moduleManager.getModules().get(clazz).iterator().next();
            initializeComponentPlayerDataClasses(module, blurPlayer);
            for (Component component : module.getSubcomponents()) {
                initializeComponentPlayerDataClasses(component, blurPlayer);
            }
        }
    }

    private void initializeComponentPlayerDataClasses(Component component, BlurPlayer blurPlayer) {
        List<Object> dataInstances = component.getPlayerMetadataCreator().initialize(blurPlayer);

        // Initialise data instances
        for (Object data : dataInstances) {
            addPlayerData(component, blurPlayer, data);
        }
    }

    public void addPlayerData(@Nonnull Component component, @Nonnull BlurPlayer blurPlayer, @Nonnull Object data) {
        Preconditions.checkNotNull(component, "component");
        Preconditions.checkNotNull(blurPlayer, "blurPlayer");
        Preconditions.checkNotNull(data, "data");

        playerMetadata.put(blurPlayer, data);
        component.addTickable(data);
        if (data instanceof PlayerData) {
            ((PlayerData) data).enable();
        }
    }

    public void removePlayer(@Nonnull BlurPlayer blurPlayer, boolean quit) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (this.players.containsKey(blurPlayer.getUuid())) {
            getLogger().finer("Removing %s from %s", blurPlayer.getName(), getName());
            BlurSession nextSession = null;
            if (!quit && !(getParentSession() instanceof RootBlurSession)) {
                nextSession = getParentSession();
            }

            // Call session switch event when nextSession is valid
            if (nextSession != null) {
                PlayerSwitchSessionEvent switchEvent = callEvent(new PlayerSwitchSessionEvent(blurPlayer, nextSession));
                if (switchEvent.isCancelled()) {
                    return;
                }
                nextSession = switchEvent.getNextSession();
            }

            callEvent(new PlayerLeaveSessionEvent(blurPlayer, this, nextSession));

            // Unregister player custom data classes.
            for (Object data : new HashSet<>(playerMetadata.getList(blurPlayer))) {
                if (data instanceof PlayerData) {
                    ((PlayerData) data).disable();
                }
                for (Class<? extends Module> moduleClass : moduleManager.getModules().keySet()) {
                    Module module = moduleManager.getModules().get(moduleClass).iterator().next();
                    module.removeTickable(data);
                }
                playerMetadata.remove(blurPlayer, data);
            }

            // If a player is removed from this session, all children should not have the same player.
            this.childrenSessions.forEach(s -> s.removePlayer(blurPlayer, quit));
            this.players.remove(blurPlayer.getUuid());
            blurPlayer.blurSession = nextSession;
            callEvent(new PlayerPostLeaveSessionEvent(blurPlayer, this));
            if (nextSession != null) {
                if (nextSession.getPlayer(blurPlayer.getUuid()).isPresent()) {
                    callEvent(new PlayerJoinSessionEvent(blurPlayer, nextSession, true));
                } else {
                    nextSession.addPlayer(blurPlayer);
                }
            }
        }
    }

    public void broadcastMessage(@Nonnull String message, Object... args) {
        Preconditions.checkNotNull(message, "message cannot be null.");
        this.players.values().forEach(p -> p.messagePrefix(message, args));
        getBlur().getPlugin().getLog().info(message, args);
    }

    public void broadcastMessage(@Nonnull net.kyori.text.Component component) {
        Preconditions.checkNotNull(component, "component cannot be null.");
        List<Player> players = this.players.values().stream().map(BlurPlayer::getPlayer).collect(Collectors.toList());
        ChatUtils.sendComponent(players, component);
        ChatUtils.sendStringComponent(Bukkit.getConsoleSender(), component);
    }

    /**
     * Returns a {@link Set} of {@link BlurPlayer} after applying the given predicate to this session's players.
     *
     * @param predicate predicate to filter the result
     *
     * @return filtered set of players
     */
    public List<BlurPlayer> getPlayers(Predicate<BlurPlayer> predicate) {
        return getPlayersStream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns a random {@link BlurPlayer} that belongs
     *
     * @param predicate predicate to filter the result
     *
     * @return filtered set of players
     */
    @Nonnull
    public Optional<BlurPlayer> getRandomPlayer(@Nonnull Predicate<BlurPlayer> predicate) {
        Preconditions.checkNotNull(predicate, "predicate cannot be null.");
        return getPlayersStream().filter(predicate).collect(CommonCollectors.singleRandom());
    }

    public Stream<BlurPlayer> getPlayersStream() {
        return players.values().stream();
    }
    
    /* ================================
     * >> GETTERS/SETTERS
     * ================================ */

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public BlurSession getParentSession() {
        return parentSession;
    }

    public Collection<BlurSession> getChildrenSessions() {
        return Collections.unmodifiableCollection(childrenSessions);
    }

    public File getRootDirectory() {
        return this.rootDirectory;
    }

    public void setRootDirectory(@Nonnull File rootDirectory) {
        this.rootDirectory = Preconditions.checkNotNull(rootDirectory, "rootDirectory cannot be null.");
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        name = name.trim();
        StringUtils.checkNotNullOrEmpty(name, "name");
        this.name = name + " session";
    }

    public ComponentState getState() {
        return state;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public int getPlayedTicks() {
        return playedTicks;
    }

    public SessionTicker getTicker() {
        return ticker;
    }

    public Map<UUID, BlurPlayer> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public net.kyori.text.Component getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(net.kyori.text.Component messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public Collection<Runnable> getOnStopTasks() {
        return Collections.unmodifiableCollection(onStopTasks);
    }

    public boolean addOnStopTask(@Nonnull Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null.");
        return this.onStopTasks.add(runnable);
    }

    public boolean removeOnStopTask(@Nonnull Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null.");
        return this.onStopTasks.remove(runnable);
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    // TODO publicize when it's fully implemented. This needs to cater for external libraries such as SupaCommons which is fixed to 20. 
    protected void setTicksPerSecond(int ticksPerSecond) {
        Preconditions.checkArgument(ticksPerSecond > 0, "ticksPerSecond must be greater than 0.");
        this.ticksPerSecond = ticksPerSecond;
        ticksAsMs = 1000 / ticksPerSecond;
    }

    private int ticksAsMs;

    public int getTicksAsMs() {
        return this.ticksAsMs;
    }

    public int millisecondsToTicks(long ms) {
        return (int) ms / ticksAsMs;
    }
    
    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    public Blur getBlur() {
        return sessionManager.getBlur();
    }

    @Nonnull
    public <T extends Module> List<T> getModule(@Nonnull Class<T> clazz) {
        return moduleManager.getModule(Preconditions.checkNotNull(clazz, "clazz cannot be null."));
    }

    public <T extends Event> T callEvent(@Nonnull T event) {
        getLogger().finest("Calling %s ", event.getClass().getSimpleName());
        return EventUtils.callEvent(Preconditions.checkNotNull(event, "event cannot be null."));
    }

    public PluginLogger getLogger() {
        return getBlur().getLogger();
    }

    public Server getServer() {
        return getBlur().getPlugin().getServer();
    }

    private boolean setState(ComponentState state) {
        if (this.state.isAcceptable(state)) {
            this.state = state;
            return true;
        }
        return false;
    }

    public MetadataStorage<BlurPlayer> getPlayerMetadata() {
        return playerMetadata;
    }

    public Map<Class<? extends SharedComponent>, SharedComponent> getSharedComponents() {
        return Collections.unmodifiableMap(sharedComponents);
    }

    @Nullable
    public <T extends SharedComponent> T getSharedComponent(Class<T> clazz) {
        return (T) sharedComponents.get(clazz);
    }

    @Nullable
    public <T extends SharedComponent> T putSharedComponent(T component) {
        getLogger().finer("Putting shared component " + component.getClass().getCanonicalName());
        T old = (T) sharedComponents.put(component.getClass(), component);
        if (old != null) {
            disableSharedComponent(old);
            unloadSharedComponent(old);
        }
        loadSharedComponent(component);
        enableSharedComponent(component);
        return old;
    }

    @Nullable
    public <T extends SharedComponent> T removeSharedComponent(Class<T> clazz) {
        getLogger().finer("Removing shared component " + clazz.getCanonicalName());
        T removed = (T) sharedComponents.remove(clazz);
        if (removed != null) {
            disableSharedComponent(removed);
            unloadSharedComponent(removed);
        }
        return removed;
    }

    public boolean loadSharedComponent(SharedComponent sharedComponent) {
        if (sharedComponent.getState().isLoaded()) {
            return false;
        }
        String name = sharedComponent.getClass().getName();
        try {
            sharedComponent.tryLoad();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error loading SharedComponent %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error loading SharedComponent " + name, e);
            }
        }
        return false;
    }

    public boolean unloadSharedComponent(SharedComponent sharedComponent) {
        if (!sharedComponent.getState().isLoaded()) {
            return false;
        }
        String name = sharedComponent.getClass().getName();
        try {
            sharedComponent.tryUnload();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error unloading SharedComponent %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error unloading SharedComponent " + name, e);
            }
        }
        return false;
    }

    public boolean enableSharedComponent(SharedComponent sharedComponent) {
        if (sharedComponent.getState() == ComponentState.ENABLED) {
            return false;
        }
        String name = sharedComponent.getClass().getName();
        try {
            sharedComponent.tryEnable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error enabling SharedComponent %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error SharedComponent SharedComponent " + name, e);
            }
        }
        return false;
    }

    public boolean disableSharedComponent(SharedComponent sharedComponent) {
        // != ENABLED means either unloaded or loaded (which are both disabled state)
        if (sharedComponent.getState() != ComponentState.ENABLED) {
            return false;
        }

        String name = sharedComponent.getClass().getName();
        try {
            sharedComponent.tryDisable();
            return true;
        } catch (Exception e) {
            if (getLogger().getDebugLevel() == 0) {
                getLogger().severe("Error disabling SharedComponent %s: %s", name, e.getMessage());
            } else {
                getLogger().log(Level.SEVERE, "Error disabling SharedComponent " + name, e);
            }
        }
        return false;
    }

    public enum Predicates implements Predicate<BlurPlayer> {
        ALIVE {
            @Override
            public boolean test(BlurPlayer blurPlayer) {
                return blurPlayer.getCoreData().isAlive();
            }
        }
    }

    private class SessionTicker extends TickerTask {

        public SessionTicker() {
            super(BlurSession.this.getBlur().getPlugin(), 0, 0);
        }

        @Override
        public void run() {
            if (!this.isPaused() && this.isStarted()) {
                BlurSession.this.playedTicks++;
            }
        }
    }
}
