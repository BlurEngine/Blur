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

import org.bukkit.scoreboard.NameTagVisibility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a state of visibility a team's nametags can be.
 */
@RequiredArgsConstructor
@Getter
public enum NametagVisibility {
    EVERYONE(NameTagVisibility.ALWAYS), NOONE(NameTagVisibility.NEVER), ALLIES(NameTagVisibility.HIDE_FOR_OTHER_TEAMS), 
    ENEMIES(NameTagVisibility.HIDE_FOR_OWN_TEAM);
    private final NameTagVisibility bukkit;
}
