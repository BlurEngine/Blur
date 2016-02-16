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

package com.blurengine.blur.serializers;

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.modules.teams.BlurTeam;
import com.blurengine.blur.serializers.TeamList.TeamListSerializer;
import com.supaham.commons.collections.PossiblyImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import pluginbase.config.annotation.SerializeWith;
import pluginbase.config.serializers.SerializerSet;

/**
 * Represents a {@link List} of {@link BlurTeam} specifically designed for easy serialization.
 */
@SerializeWith(TeamListSerializer.class)
public class TeamList extends PossiblyImmutableList<BlurTeam> {

    public static final TeamList EMPTY = new TeamList();

    public TeamList() {
        super();
    }

    public TeamList(int initialCapacity) {
        super(new ArrayList<>(initialCapacity));
    }

    public TeamList(Collection<? extends BlurTeam> c) {
        super(new ArrayList<>(c));
    }

    public static final class TeamListSerializer implements BlurSerializer<TeamList> {

        @Override
        public TeamList deserialize(Object serialized, Class wantedType, SerializerSet serializerSet) throws IllegalArgumentException {
            if (serialized == null) {
                return EMPTY;
            }
            pluginbase.config.serializers.Serializer<BlurTeam> ser = serializerSet.getClassSerializer(BlurTeam.class);
            if (serialized instanceof List) {
                return new TeamList(((List<Object>) serialized).stream().map(o -> ser.deserialize(o, BlurTeam.class, serializerSet)).collect(Collectors.toList()));
            } else {
                return new TeamList(Collections.singletonList(ser.deserialize(serialized, BlurTeam.class, serializerSet)));
            }
        }
    }
}
