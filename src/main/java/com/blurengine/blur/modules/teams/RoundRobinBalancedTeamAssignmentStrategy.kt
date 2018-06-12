/*
 * Copyright 2018 Ali Moghnieh
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

package com.blurengine.blur.modules.teams

import com.blurengine.blur.session.BlurPlayer
import com.blurengine.blur.utils.allMinBy
import java.util.function.Supplier

class RoundRobinBalancedTeamAssignmentStrategy(override val teams: Supplier<Collection<BlurTeam>>) : TeamAssignmentStrategy {
    override fun getTeam(blurPlayer: BlurPlayer): BlurTeam? {
        val teams = teams.get()
        val minPlayerTeams = teams
                .map { it to it.playerCount }
                .allMinBy { (_, playerCount) -> playerCount }
                .map { it.first }
        return minPlayerTeams.first()
    }
}
