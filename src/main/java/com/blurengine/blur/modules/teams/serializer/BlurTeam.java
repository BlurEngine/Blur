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

package com.blurengine.blur.modules.teams.serializer;

import com.blurengine.blur.modules.teams.NametagVisibility;
import com.blurengine.blur.modules.filters.Filter;
import com.supaham.commons.bukkit.serializers.ColorStringSerializer;
import com.supaham.commons.utils.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import javax.annotation.Nonnull;

import lombok.ToString;
import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.SerializeWith;

/**
 * Represents a team that is utilized by {@link BlurTeam}.
 */
@ToString
public class BlurTeam implements Comparable<BlurTeam>, Filter {

    private String id;
    private String name = null;
    @Name("chat-prefix")
    @SerializeWith(ColorStringSerializer.class)
    private String chatPrefix = ChatColor.WHITE.toString();
    private Color color = Color.WHITE;
    private int max = 10;
    @Name("max-overfill")
    private int maxOverfill = 12;
    @Name("nametag-visibility")
    private NametagVisibility nametagVisibility = NametagVisibility.EVERYONE;

    public static Builder builder() {
        return new Builder();
    }

    BlurTeam() {}

    public BlurTeam(@Nonnull Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.chatPrefix = builder.chatPrefix;
        this.color = builder.color;
        this.max = builder.max;
        this.maxOverfill = builder.maxOverfill;
        this.nametagVisibility = builder.nametagVisibility;
    }

    @Override
    public int compareTo(BlurTeam o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlurTeam && id.equals(((BlurTeam) obj).id);
    }

    @Override
    public FilterResponse test(Object object) {
        if (object instanceof BlurTeam) {
            return FilterResponse.from(this.equals(object));
            // TODO handle players
        }
        return FilterResponse.ABSTAIN;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

        public BlurTeam build() {
            StringUtils.checkNotNullOrEmpty(id, "id");
            if (name == null) {
                name = id;
            }
            return new BlurTeam(this);
        }
    }
}
