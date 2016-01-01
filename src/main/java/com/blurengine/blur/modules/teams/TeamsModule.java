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

import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.teams.TeamsModule.TeamsData;
import com.blurengine.blur.modules.teams.serializer.BlurTeam;
import com.blurengine.blur.modules.teams.serializer.TeamSerializer.ListTeamSerializer;

import java.util.List;
import java.util.Map;

import pluginbase.config.annotation.SerializeWith;

/**
 * Represents a {@link Module} that allows for the creation of {@link Filter} and nothing else. Intended for user convenience.
 */
@ModuleInfo(name = "Teams", dataClass = TeamsData.class)
public class TeamsModule extends Module {

    private TeamsModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public static final class TeamsData implements ModuleData {

        @SerializeWith(ListTeamSerializer.class)
        private List<BlurTeam> teams;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof Map) {
                moduleManager.getModuleLoader().getTeamSerializer().deserialize(serialized.getAsObject(), null);
            } else {
                serialized.load(this);
            }
            return new TeamsModule(moduleManager);
        }
    }
}
