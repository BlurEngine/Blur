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

import com.blurengine.blur.modules.framework.InternalModule;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleLoader;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.teams.serializer.BlurTeam;
import com.blurengine.blur.session.BlurSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a {@link BlurTeam} manager. This manager contains all teams registered by a {@link ModuleManager} meaning these same teams can be 
 * reused in child {@link BlurSession}.
 */
@ModuleInfo(name = "BlurTeamManager")
@InternalModule
public class TeamManager extends Module {

    public static final String FILTER_PREFIX = "team-";

    private Map<String, BlurTeam> teams = new HashMap<>();

    static {
        ModuleLoader.register(TeamsModule.class);
    }

    public TeamManager(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public Collection<BlurTeam> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public BlurTeam getTeamById(String id) {
        return this.teams.get(id);
    }

    public void addTeam(BlurTeam blurTeam) {
        Preconditions.checkArgument(!this.teams.containsKey(blurTeam.getId()), "team with id '%s' already exists.", blurTeam.getId());
        getModuleManager().getFilterManager().addFilter(FILTER_PREFIX + blurTeam.getId(), blurTeam);
        this.teams.put(blurTeam.getId(), blurTeam);
    }
}
