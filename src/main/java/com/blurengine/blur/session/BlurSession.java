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
import com.blurengine.blur.events.session.SessionEnableEvent;
import com.blurengine.blur.events.session.SessionStartEvent;
import com.blurengine.blur.events.session.SessionStopEvent;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.bukkit.scoreboards.CommonScoreboard;
import com.supaham.commons.bukkit.text.FancyMessage;
import com.supaham.commons.bukkit.text.MessagePart;
import com.supaham.commons.bukkit.utils.EventUtils;
import com.supaham.commons.utils.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.io.File;
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
    private final Set<BlurSession> childrenSessions = new HashSet<>();
    private File rootDirectory = new File(".");
    private String name = getClass().getSimpleName(); // Default session name to short class name

    private final CommonScoreboard scoreboard;
    private boolean enabled;
    private boolean started;
    private boolean paused;

    private final Map<UUID, BlurPlayer> players = new HashMap<>();

    private MessagePart messagePrefixMP = new MessagePart("");

    private final List<Runnable> onStopTasks = new ArrayList<>();

    {
        // Bukkit hasn't fully initialized yet (this is the RootBlurSession)
        if (Bukkit.getScoreboardManager() != null) {
            this.scoreboard = new CommonScoreboard();
        } else {
            this.scoreboard = null;
        }
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

    public <T extends BlurSession> T addChildSession(T session) {
        // Future precaution, this will never be the case as of the time of writing this as equals() has not been overridden.
        Preconditions.checkState(this.childrenSessions.add(session), "New session somehow already exists.");
        return session;
    }

    public void enable() {
        if (enabled) {
            return;
        }
        getLogger().fine("Enabling %s", getName());
        long startedAt = System.currentTimeMillis();
        this.moduleManager.load();
        this.moduleManager.enable();
        this.enabled = true;
        // Delay event by a tick to give the server time to catch up if it took too long loading the session.
        new TickerTask(getBlur().getPlugin(), 1, () -> callEvent(new SessionEnableEvent(this))).start();
        getLogger().fine("%s enabled in %dms", getName(), System.currentTimeMillis() - startedAt);
    }

    public void start() {
        if (!this.enabled) {
            enable();
        }
        getLogger().fine("Starting %s", getName());
        long startedAt = System.currentTimeMillis();
        callEvent(new SessionStartEvent(this));
        this.started = true;
        getLogger().fine("%s started in %dms", getName(), System.currentTimeMillis() - startedAt);
    }

    public void stop() {
        if (!started) {
            return;
        }
        getLogger().fine("Stopping %s", getName());
        long startedAt = System.currentTimeMillis();
        this.enabled = false;
        this.started = false;
        callEvent(new SessionStopEvent(this));
        this.childrenSessions.forEach(BlurSession::stop);
        this.moduleManager.disable();
        this.moduleManager.unload();
        this.onStopTasks.forEach(Runnable::run);
        getLogger().fine("%s stopped in %dms", getName(), System.currentTimeMillis() - startedAt);
    }

    public BlurPlayer getPlayer(@Nonnull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null.");
        return getPlayer(player.getUniqueId()).orElse(null); // 99.9% this will never be null as a Player is always online and has a BlurPlayer instance.
    }

    public Optional<BlurPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(this.players.get(uuid));
    }

    public void addPlayer(@Nonnull BlurPlayer blurPlayer) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (!this.players.containsKey(blurPlayer.getUuid())) {
            getLogger().finer("Adding %s to %s", blurPlayer.getName(), getName());
            this.players.put(blurPlayer.getUuid(), blurPlayer);
            callEvent(new PlayerJoinSessionEvent(blurPlayer, this));
        }
    }

    public void removePlayer(@Nonnull BlurPlayer blurPlayer) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (this.players.containsKey(blurPlayer.getUuid())) {
            getLogger().finer("Removing %s from %s", blurPlayer.getName(), getName());
            this.players.remove(blurPlayer.getUuid());
            callEvent(new PlayerLeaveSessionEvent(blurPlayer, this));
        }
    }

    public void broadcastMessage(@Nonnull String message, Object... args) {
        Preconditions.checkNotNull(message, "message cannot be null.");
        this.players.values().forEach(p -> p.messagePrefix(message, args));
        getBlur().getPlugin().getLog().info(message, args);
    }

    public void broadcastMessage(@Nonnull FancyMessage fancyMessage) {
        Preconditions.checkNotNull(fancyMessage, "fancyMessage cannot be null.");
        fancyMessage.send(players.values().stream().map(BlurPlayer::getPlayer).collect(Collectors.toList()));
        getBlur().getPlugin().getLog().info(fancyMessage.toReadableString());
    }

    /**
     * Returns a {@link Set} of {@link BlurPlayer} after applying the given predicate to this session's players.
     *
     * @param predicate predicate to filter the result
     *
     * @return filtered set of players
     */
    public Set<BlurPlayer> getPlayers(Predicate<BlurPlayer> predicate) {
        return getPlayersStream().filter(predicate).collect(Collectors.toSet());
    }

    /**
     * Returns a random {@link BlurPlayer} that belongs
     *
     * @param predicate predicate to filter the result
     *
     * @return filtered set of players
     */
    public Optional<BlurPlayer> getAnyPlayer(Predicate<BlurPlayer> predicate) {
        return getPlayersStream().filter(predicate).findAny();
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

    public CommonScoreboard getScoreboard() {
        return scoreboard;
    }

    public boolean isEnabled() {
        return enabled;
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

    public Map<UUID, BlurPlayer> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public MessagePart getMessagePrefixMP() {
        return messagePrefixMP;
    }

    public void setMessagePrefixMP(MessagePart messagePrefixMP) {
        this.messagePrefixMP = messagePrefixMP;
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

    public enum Predicates implements Predicate<BlurPlayer> {
        ALIVE {
            @Override
            public boolean test(BlurPlayer blurPlayer) {
                return blurPlayer.isAlive();
            }
        }
    }
}
