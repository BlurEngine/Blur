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

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.framework.BlurSerializer;
import com.blurengine.blur.modules.teams.NametagVisibility;
import com.blurengine.blur.modules.framework.ModuleLoader;
import com.blurengine.blur.modules.teams.TeamManager;
import com.supaham.commons.bukkit.serializers.ColorStringSerializer;
import com.supaham.commons.serializers.ListSerializer;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.SerializeWith;
import pluginbase.config.serializers.SerializerSet;

public class TeamSerializer implements BlurSerializer<BlurTeam> {

    private final ModuleLoader moduleLoader;

    public TeamSerializer(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
    }

    @Override
    public BlurTeam deserialize(@Nullable Object serialized, @NotNull Class wantedType, @NotNull SerializerSet serializerSet) {
        if (serialized == null) {
            return null;
        } else if (serialized instanceof Map) {
            return deserializeTeam((Map<String, Object>) serialized);
        } else if (serialized instanceof String) {
            BlurTeam blurTeam = getManager().getTeamById(serialized.toString());
            return Preconditions.checkNotNull(blurTeam, "Could not find team by id '%s'.", serialized);
        }
        throw new IllegalArgumentException("Expected List or Map, got data type of " + serialized.getClass().getName() + ".");
    }

    public BlurTeam deserializeTeam(Map<String, Object> map) {

        Optional<String> id = Optional.ofNullable((String) map.get("id"));

        BlurTeam blurTeam = null;
        if (map.size() > 1 && id.isPresent()) {
            id.orElseThrow(() -> new IllegalArgumentException("Team must have an id."));
            blurTeam = this.moduleLoader.deserializeTo(map, new BlurTeamData()).toTeam();
        }

        // If no team was defined, then we must have an id reference
        if (blurTeam == null) {
            String teamId = id.orElseThrow(() -> new NullPointerException("no blurTeam id or blurTeam definition."));
            blurTeam = getManager().getTeamById(teamId);
            Preconditions.checkNotNull(blurTeam, "Could not find team by id '%s'.", teamId);
        }

        getManager().addTeam(blurTeam);
        return blurTeam;
    }
    
    public TeamManager getManager() {
        return this.moduleLoader.getModuleManager().getTeamManager();
    }

    public static class ListTeamSerializer extends ListSerializer<BlurTeam> {

        @Override
        public Class<BlurTeam> getTypeClass() {
            return BlurTeam.class;
        }
    }

    private static final class BlurTeamData {

        private String id;
        private String name = null;
        @Name("chat-prefix")
        @SerializeWith(ColorStringSerializer.class)
        private String chatPrefix = ChatColor.WHITE.toString();
        private Color color = Color.WHITE;
        private int max = 100;
        @Name("max-overfill")
        private int maxOverfill = 120;
        @Name("nametag-visibility")
        private NametagVisibility nametagVisibility = NametagVisibility.EVERYONE;

        public BlurTeam toTeam() {
            return BlurTeam.builder().id(id).name(name).chatPrefix(chatPrefix).color(color).max(max).maxOverfill(maxOverfill)
                .nametagVisibility(nametagVisibility).build();
        }
    }
}
