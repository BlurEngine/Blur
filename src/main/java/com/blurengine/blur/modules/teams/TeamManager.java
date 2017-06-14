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

package com.blurengine.blur.modules.teams;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.blurengine.blur.events.players.PlayerJoinSessionEvent;
import com.blurengine.blur.framework.InternalModule;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.modules.teams.events.PlayerChangeTeamEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.supervisor.Amendable;
import com.blurengine.blur.supervisor.SupervisorContext;
import com.supaham.commons.CommonCollectors;
import com.supaham.commons.utils.BeanUtils;

import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents a {@link BlurTeam} manager. This manager contains all teams registered by a {@link ModuleManager} meaning these same teams can be 
 * reused in child {@link BlurSession}.
 */
@ModuleInfo(name = "BlurTeamManager")
@InternalModule
public class TeamManager extends Module implements SupervisorContext {

    public static final String FILTER_PREFIX = "team-";
    private SpectatorTeam spectatorTeam;

    private Map<String, BlurTeam> teams = new HashMap<>();
    private Map<BlurPlayer, BlurTeam> playerTeams = new HashMap<>();

    static {
        ModuleLoader.register(TeamsModule.class);
    }

    public TeamManager(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public Optional<BlurTeam> getTeamById(@Nonnull String id) {
        Preconditions.checkNotNull(id, "id cannot be null.");
        return Optional.ofNullable(this.teams.get(id));
    }

    public void registerTeam(@Nonnull BlurTeam blurTeam) {
        Preconditions.checkNotNull(blurTeam, "blurTeam cannot be null.");
        Preconditions.checkArgument(!this.teams.containsKey(blurTeam.getId()), "team with id '%s' already exists.", blurTeam.getId());
        getLogger().finer("Registering team %s", blurTeam.getId());
        getModuleManager().getFilterManager().addFilter(FILTER_PREFIX + blurTeam.getId(), blurTeam);
        this.teams.put(blurTeam.getId(), blurTeam);
    }

    @Nonnull
    public BlurTeam getPlayerTeam(BlurPlayer blurPlayer) {
        return this.playerTeams.get(blurPlayer);
    }

    public boolean setPlayerTeam(@Nonnull BlurPlayer blurPlayer, BlurTeam blurTeam) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        BlurTeam oldTeam = this.playerTeams.get(blurPlayer);
        if (Objects.equal(blurTeam, oldTeam)) {
            return false;
        }
        getLogger().finer("Setting %s team to %s.", blurPlayer.getName(), blurTeam.getId());
        PlayerChangeTeamEvent event = getSession().callEvent(new PlayerChangeTeamEvent(blurPlayer, oldTeam, blurTeam));
        if (event.isCancelled()) {
            return false;
        }
        if (oldTeam != null) {
            oldTeam.players.remove(blurPlayer);
            if (oldTeam.bukkitTeam != null) {
                oldTeam.bukkitTeam.removeEntry(blurPlayer.getName());
            }
        }
        blurTeam = event.getNewTeam().orElse(null);
        if (blurTeam != null) {
            this.playerTeams.put(blurPlayer, blurTeam);
            blurTeam.players.add(blurPlayer);
            blurTeam.bukkitTeam.addEntry(blurPlayer.getName());
        } else {
            this.playerTeams.remove(blurPlayer);
        }
        return true;
    }

    public SpectatorTeam getSpectatorTeam() {
        return spectatorTeam;
    }

    public void setSpectatorTeam(@Nonnull SpectatorTeam spectatorTeam) {
        Preconditions.checkNotNull(spectatorTeam, "spectatorTeam cannot be null.");
        Preconditions.checkState(this.spectatorTeam == null, "spectatorTeam has already been set.");
        this.spectatorTeam = spectatorTeam;
        // The reason for not calling registerTeam is that SpectatorTeam will always be present in all games and could be obnoxious every time 
        // a user calls #getTeam() with the intention of getting teams related to the gameplay.
        getModuleManager().getFilterManager().addFilter(FILTER_PREFIX + spectatorTeam.getId(), spectatorTeam);
    }

    public Collection<BlurTeam> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public Collection<BlurTeam> getTeamsWithSpectator() {
        ArrayList<BlurTeam> result = new ArrayList<>(this.teams.values());
        result.add(this.spectatorTeam);
        return result;
    }

    @EventHandler
    public void onPlayerJoinSession(PlayerJoinSessionEvent event) {
        // TODO make initial team setting optional. E.g. if they game has already started, set them to spectators only.
        if (isSession(event.getSession()) && getSpectatorTeam() != null) { // spectator team is null because this might be root session.
            List<BlurTeam> smallestTeams = new ArrayList<>(teams.size());
            int size = Integer.MAX_VALUE;
            for (BlurTeam blurTeam : getTeams()) {
                if (blurTeam.getPlayerCount() < size) {
                    smallestTeams.clear();
                    size = blurTeam.getPlayerCount();
                }
                if (blurTeam.getPlayerCount() == size) {
                    smallestTeams.add(blurTeam);
                }
            }
            BlurTeam blurTeam = smallestTeams.stream().collect(CommonCollectors.singleRandom()).get();
            getLogger().finer("Adding %s to team %s with size %s", event.getBlurPlayer().getDisplayName(), blurTeam.getId(), size);
            blurTeam.addPlayer(event.getBlurPlayer());
        }
    }

    /* ================================
     * >> SUPERVISOR
     * ================================ */

    @Override
    public void run(@Nonnull Amendable amendable) {
        amendable.append("teams", teams.values().stream().map(this::team).collect(Collectors.toList()));
        amendable.append("player_teams", playerTeams.entrySet().stream()
            .map(e -> Collections.singletonMap(e.getKey().getName(), e.getValue().getId())).collect(Collectors.toList()));
    }

    private Object team(BlurTeam blurTeam) {
        return BeanUtils.getPropertiesList(blurTeam, getModuleManager().getModuleLoader().getSerializerSet());
    }
}
