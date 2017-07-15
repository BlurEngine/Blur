/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.framework.metadata.teamdata;

import com.blurengine.blur.framework.metadata.auto.MetadataCreator;
import com.blurengine.blur.modules.teams.BlurTeam;

import javax.annotation.Nonnull;

/**
 * Team data class supplier. This does not require that the supplied class be of type {@link TeamData}.
 */
public interface TeamDataCreator<T> extends MetadataCreator<T, BlurTeam> {

    /**
     * Returns a new instance of Team data for the given {@link BlurTeam}.
     * @param blurTeam blur team to create data for
     * @return new team data instance
     */
    @Nonnull
    T create(BlurTeam blurTeam);
}
