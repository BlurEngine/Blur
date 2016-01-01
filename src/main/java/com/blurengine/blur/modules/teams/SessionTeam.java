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

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.teams.events.TeamRenameEvent;
import com.blurengine.blur.modules.teams.serializer.BlurTeam;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;
import com.supaham.commons.bukkit.utils.EventUtils;

import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a team that may be present during a {@link BlurSession}.
 */
public class SessionTeam {

    private final TeamManager manager;
    private final BlurTeam blurTeam;
    private final Set<BlurPlayer> players = new HashSet<>();
    private final Team bukkitTeam;
    private String name;

    public SessionTeam(TeamManager manager, BlurTeam blurTeam) {
        this.manager = manager;
        this.blurTeam = blurTeam;
        this.name = blurTeam.getName();
        this.bukkitTeam = manager.getSession().getScoreboard().getBukkitScoreboard().registerNewTeam(blurTeam.getName());
        this.bukkitTeam.setDisplayName(getName());
        this.bukkitTeam.setPrefix(this.blurTeam.getChatPrefix());
        this.bukkitTeam.setNameTagVisibility(blurTeam.getNametagVisibility().getBukkit());
    }

    public boolean addPlayer(@Nonnull BlurPlayer blurPlayer) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (this.players.add(blurPlayer)) {
            this.bukkitTeam.addPlayer(blurPlayer.getPlayer());
            return true;
        }
        return false;
    }

    public boolean removePlayer(@Nonnull BlurPlayer blurPlayer) {
        Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        if (this.players.remove(blurPlayer)) {
            if (this.bukkitTeam != null) {
                this.bukkitTeam.removePlayer(blurPlayer.getPlayer());
            }
            return true;
        }
        return false;
    }

    public TeamManager getManager() {
        return manager;
    }

    public BlurTeam getBlurTeam() {
        return blurTeam;
    }

    public Collection<BlurPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public Team getBukkitTeam() {
        return bukkitTeam;
    }

    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        String oldName = getName();
        if (Objects.equals(this.name, name) || getName().equals(name)) {
            return;
        }
        this.name = name;
        this.bukkitTeam.setDisplayName(this.blurTeam.getChatPrefix() + name);
        EventUtils.callEvent(new TeamRenameEvent(this, oldName, name));
    }

    public boolean isFull() {
        return this.players.size() >= this.blurTeam.getMax();
    }

    public boolean isOverfilled() {
        return this.players.size() >= this.blurTeam.getMaxOverfill();
    }
}
