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

import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.teams.events.TeamRenameEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.utils.EventUtils;
import com.supaham.commons.utils.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a team that is utilized by {@link BlurTeam}.
 */
public class BlurTeam implements Comparable<BlurTeam>, Filter {

    private final String id;
    private String name;
    private final String chatPrefix;
    private final Color color;
    private final int max;
    private final int maxOverfill;
    private final NametagVisibility nametagVisibility;

    private final transient TeamManager manager;
    final transient Team bukkitTeam;
    final transient Set<BlurPlayer> players = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    public BlurTeam(@Nonnull TeamManager teamManager, @Nonnull Builder builder) {
        Preconditions.checkNotNull(teamManager, "teamManager cannot be null.");
        Preconditions.checkNotNull(builder, "builder cannot be null.");

        this.manager = teamManager;

        this.id = builder.id;
        this.name = builder.name;
        this.chatPrefix = builder.chatPrefix;
        this.color = builder.color;
        this.max = builder.max;
        this.maxOverfill = builder.maxOverfill;
        this.nametagVisibility = builder.nametagVisibility;

        this.bukkitTeam = manager.getSession().getScoreboard().getBukkitScoreboard().registerNewTeam(getId());
        this.bukkitTeam.setDisplayName(getName());
        this.bukkitTeam.setPrefix(getChatPrefix());
        this.bukkitTeam.setOption(Option.NAME_TAG_VISIBILITY, getNametagVisibility().getBukkit());
    }

    @Override
    public int compareTo(@Nonnull BlurTeam o) {
        Preconditions.checkNotNull(o, "o cannot be null.");
        return getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlurTeam && id.equals(((BlurTeam) obj).id);
    }

    @Override
    public String toString() {
        return "BlurTeam{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", chatPrefix='" + chatPrefix + '\'' +
            ", color=" + color +
            ", max=" + max +
            ", maxOverfill=" + maxOverfill +
            ", nametagVisibility=" + nametagVisibility +
            '}';
    }

    @Override
    public FilterResponse test(Object object) {
        if (object instanceof BlurTeam) {
            return FilterResponse.from(this.equals(object));
        } else if (object instanceof BlurPlayer) {
            return FilterResponse.from(this.players.contains(object));
        } else if (object instanceof Player) {
            return FilterResponse.from(this.players.contains(manager.getPlayer((Player) object)));
        }
        return FilterResponse.ABSTAIN;
    }

    public boolean addPlayer(@Nonnull BlurPlayer blurPlayer) {
        return manager.setPlayerTeam(blurPlayer, this);
    }

    public boolean removePlayer(@Nonnull BlurPlayer blurPlayer) {
        return manager.setPlayerTeam(blurPlayer, null);
    }

    public Collection<BlurPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public TeamManager getManager() {
        return manager;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        String oldName = getName();
        if (Objects.equals(this.name, name) || getName().equals(name)) {
            return;
        }
        this.name = name;
        this.bukkitTeam.setDisplayName(getChatPrefix() + name);
        EventUtils.callEvent(new TeamRenameEvent(this, oldName, name));
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public Color getColor() {
        return color;
    }

    public int getMax() {
        return max;
    }

    public int getMaxOverfill() {
        return maxOverfill;
    }

    public boolean isFull() {
        return this.players.size() >= getMax();
    }

    public boolean isOverfilled() {
        return this.players.size() >= getMaxOverfill();
    }

    public NametagVisibility getNametagVisibility() {
        return nametagVisibility;
    }

    public static final class Builder {

        private String id;
        private String name;
        private String chatPrefix = ChatColor.WHITE.toString();
        private Color color = Color.WHITE;
        private int max = 10;
        private int maxOverfill = 12;
        private NametagVisibility nametagVisibility = NametagVisibility.EVERYONE;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder chatPrefix(String chatPrefix) {
            this.chatPrefix = chatPrefix;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder maxOverfill(int maxOverfill) {
            this.maxOverfill = maxOverfill;
            return this;
        }

        public Builder nametagVisibility(NametagVisibility nametagVisibility) {
            this.nametagVisibility = nametagVisibility;
            return this;
        }

        public BlurTeam build(@Nonnull TeamManager teamManager) {
            Preconditions.checkNotNull(teamManager, "teamManager cannot be null.");
            StringUtils.checkNotNullOrEmpty(id, "id");
            if (name == null) {
                name = id;
            }
            return new BlurTeam(teamManager, this);
        }
    }
}
