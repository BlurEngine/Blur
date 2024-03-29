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

import com.blurengine.blur.serializers.ComponentSerializer;
import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.metadata.MetadataHolder;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.teams.events.TeamRenameEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.utils.StringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import pluginbase.config.annotation.SerializeWith;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a team that is utilized by {@link BlurTeam}.
 */
public class BlurTeam implements Comparable<BlurTeam>, Filter, MetadataHolder {

    private final String id;
    private String name;
    private final net.md_5.bungee.api.ChatColor chatColor;
    @SerializeWith(ComponentSerializer.class)
    private final BaseComponent chatPrefix;
    private final Color color;
    private final int max;
    private final int maxOverfill;
    private final NametagVisibility nametagVisibility;
    private final NametagVisibility deathMessageVisibility;
    private final CollisionRule collisionRule;

    private final transient TeamManager manager;
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
        this.chatColor = builder.chatColor;
        this.chatPrefix = builder.chatPrefix;
        this.color = builder.color;
        this.max = builder.max;
        this.maxOverfill = builder.maxOverfill;
        this.nametagVisibility = builder.nametagVisibility;
        this.deathMessageVisibility = builder.deathMessageVisibility;
        this.collisionRule = builder.collisionRule;
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
            ", chatColor='" + chatColor +
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

    public void updateTeamFields(@Nonnull Team team, boolean updatePlayers) {
        if (!team.getDisplayName().equals(getName())) {
            team.setDisplayName(getName());
        }
        if (!team.getPrefix().equals(getChatPrefixLegacy())) {
            team.setPrefix(getChatPrefixLegacy());
        }
        if (!team.getOption(Option.NAME_TAG_VISIBILITY).equals(getNametagVisibility().getBukkit())) {
            team.setOption(Option.NAME_TAG_VISIBILITY, getNametagVisibility().getBukkit());
        }
        if (!team.getOption(Option.DEATH_MESSAGE_VISIBILITY).equals(getDeathMessageVisibility().getBukkit())) {
            team.setOption(Option.DEATH_MESSAGE_VISIBILITY, getDeathMessageVisibility().getBukkit());
        }
        if (!team.getOption(Option.COLLISION_RULE).equals(getCollisionRule().getBukkit())) {
            team.setOption(Option.COLLISION_RULE, getCollisionRule().getBukkit());
        }
        team.setColor(ChatColor.valueOf(getChatColor().getName().toUpperCase()));
        if (updatePlayers) {
            Set<String> oldEntries = players.stream().map(BlurPlayer::getName).collect(Collectors.toSet());
            oldEntries.removeAll(team.getEntries());
            oldEntries.forEach(team::removeEntry);
            for (BlurPlayer blurPlayer : players) {
                team.addEntry(blurPlayer.getName());
            }
        }
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
        getManager().getSession().callEvent(new TeamRenameEvent(this, oldName, name));
    }

    public net.md_5.bungee.api.ChatColor getChatColor() {
        return chatColor;
    }

    public BaseComponent getChatPrefix() {
        return chatPrefix;
    }

    public String getChatPrefixLegacy() {
        return chatPrefix.toLegacyText();
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

    public NametagVisibility getDeathMessageVisibility() {
        return deathMessageVisibility;
    }

    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Returns the {@link #getChatColor()} + {@link #getName()}.
     * @return display name
     */
    public String getChatDisplayName() {
        ChatColor bukkitChatColor = ChatColor.valueOf(this.chatColor.getName().toUpperCase());
        return bukkitChatColor + this.name;
    }

    @Override
    public boolean hasMetadata(Object object) {
        return getManager().getTeamMetadata().contains(this, object);
    }

    @Override
    public <T> boolean hasMetadata(Class<T> metadataClass) {
        return getManager().getTeamMetadata().contains(this, metadataClass);
    }

    @Override
    public <T> T getMetadata(Class<T> metadataClass) {
        return getManager().getTeamMetadata().get(this, metadataClass);
    }

    @Override
    public Object putMetadata(Object object) {
        return getManager().getTeamMetadata().put(this, object);
    }

    @Nonnull
    @Override
    public List<Object> removeAll() {
        return getManager().getTeamMetadata().removeAll(this);
    }

    @Override
    public <T> boolean removeMetadata(T object) {
        return getManager().getTeamMetadata().remove(this, object);
    }

    @Nullable
    @Override
    public <T> T removeMetadata(Class<T> metadataClass) {
        return getManager().getTeamMetadata().remove(this, metadataClass);
    }

    public static final class Builder {

        private String id;
        private String name;
        private net.md_5.bungee.api.ChatColor chatColor;
        private TextComponent chatPrefix = new TextComponent(new ComponentBuilder("").color(net.md_5.bungee.api.ChatColor.WHITE).create());
        private Color color = Color.WHITE;
        private int max = 10;
        private int maxOverfill = 12;
        private NametagVisibility nametagVisibility = NametagVisibility.EVERYONE;
        private NametagVisibility deathMessageVisibility = NametagVisibility.EVERYONE;
        private CollisionRule collisionRule = CollisionRule.EVERYONE;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder chatColor(net.md_5.bungee.api.ChatColor chatColor) {
            this.chatColor = chatColor;
            return this;
        }

        public Builder chatPrefix(TextComponent chatPrefix) {
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

        public Builder deathMessageVisibility(NametagVisibility nametagVisibility) {
            this.deathMessageVisibility = nametagVisibility;
            return this;
        }

        public Builder collisionRule(CollisionRule collisionRule) {
            this.collisionRule = collisionRule;
            return this;
        }

        public BlurTeam build(@Nonnull TeamManager teamManager) {
            Preconditions.checkNotNull(teamManager, "teamManager cannot be null.");
            StringUtils.checkNotNullOrEmpty(id, "id");
            Preconditions.checkNotNull(color, "color cannot be null.");
            if (chatColor == null) {
                chatColor = net.md_5.bungee.api.ChatColor.of(color.toString());
            }
            if (name == null) {
                name = id;
            }
            return new BlurTeam(teamManager, this);
        }
    }
}
