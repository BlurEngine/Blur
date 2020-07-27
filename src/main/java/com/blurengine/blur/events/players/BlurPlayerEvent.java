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

    public BlurPlayerEvent(@Nonnull BlurPlayer blurPlayer) {
        this(Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null."), blurPlayer.getSession());
    }

    public BlurPlayerEvent(@Nonnull BlurPlayer blurPlayer, boolean isAsync) {
        this(Preconditions.checkNotNull(blurPlayer, "blurPlayer cannot be null."), blurPlayer.getSession(), isAsync);
    }

    public BlurPlayerEvent(BlurPlayer blurPlayer, @Nonnull BlurSession session) {
        super(Preconditions.checkNotNull(session, "session cannot be null."));
        this.blurPlayer = blurPlayer;
    }

    public BlurPlayerEvent(BlurPlayer blurPlayer, @Nonnull BlurSession session, boolean isAsync) {
        super(Preconditions.checkNotNull(session, "session cannot be null."), isAsync);
        this.blurPlayer = blurPlayer;
    }

    /**
     * Returns the primary {@link BlurPlayer} involved in this event. Subclass events will have delegate methods that represent what this primary
     * player is.
     * <p />
     * E.g. {@link PlayerDamagePlayerEvent} returns the damager. {@link PlayerKilledEvent} returns the player that was killed.
     * @return primary blur player
     */
    public BlurPlayer getBlurPlayer() {
        return blurPlayer;
    }
}
