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

package com.blurengine.blur.events.players;

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.session.BlurSessionEvent;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;

import javax.annotation.Nonnull;

/**
 * Represents a {@link BlurPlayer} event.
 */
public abstract class BlurPlayerEvent extends BlurSessionEvent {

    protected final BlurPlayer blurPlayer;

    public BlurPlayerEvent(BlurPlayer blurPlayer, @Nonnull BlurSession session) {
        super(Preconditions.checkNotNull(session, "session cannot be null."));
        this.blurPlayer = blurPlayer;
    }

    public BlurPlayer getBlurPlayer() {
        return blurPlayer;
    }
}
