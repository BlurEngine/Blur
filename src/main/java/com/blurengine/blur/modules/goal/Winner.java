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

package com.blurengine.blur.modules.goal;

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.teams.BlurTeam;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;

/**
 * Represents a winner of a {@link BlurSession}.
 */
public interface Winner {

    String getName();

    class NullWinner implements Winner {

        public static final NullWinner INSTANCE = new NullWinner();

        @Override
        public String getName() {
            return "null";
        }
    }

    class PlayerWinner implements Winner {

        private final BlurPlayer blurPlayer;

        public PlayerWinner(BlurPlayer blurPlayer) {
            this.blurPlayer = Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null.");
        }

        @Override
        public String getName() {
            return blurPlayer.getName();
        }

        public BlurPlayer getBlurPlayer() {
            return blurPlayer;
        }
    }

    class TeamWinner implements Winner {

        private final BlurTeam team;

        public TeamWinner(BlurTeam team) {
            this.team = Preconditions.checkNotNull(team, "team cannot be null.");
        }

        @Override
        public String getName() {
            return team.getName();
        }

        public BlurTeam getTeam() {
            return team;
        }
    }
}
