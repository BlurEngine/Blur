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

import com.blurengine.blur.modules.teams.serializer.BlurTeam;
import com.blurengine.blur.session.BlurPlayer;

import javax.annotation.Nonnull;

/**
 * Represents a {@link SessionTeam} designed for spectating players.
 */
public final class SpectatorTeam extends SessionTeam {

    public static final String TEAM_ID = "spectators";

    public SpectatorTeam(TeamManager manager) {
        super(manager, BlurTeam.builder().id(TEAM_ID).name("Spectators").max(9001).maxOverfill(9001).build());
    }

    @Override
    public boolean addPlayer(@Nonnull BlurPlayer gamePlayer) {
        return super.addPlayer(gamePlayer);
    }
}