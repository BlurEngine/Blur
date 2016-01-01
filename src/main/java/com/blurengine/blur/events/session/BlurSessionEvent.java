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

package com.blurengine.blur.events.session;

import com.google.common.base.Preconditions;

import com.blurengine.blur.session.BlurSession;

import org.bukkit.event.Event;

/**
 * Represents a {@link BlurSession} event.
 */
public abstract class BlurSessionEvent extends Event {

    protected final BlurSession blurSession;

    public BlurSessionEvent(BlurSession blurSession) {
        Preconditions.checkNotNull(blurSession, "session cannot be null.");
        this.blurSession = blurSession;
    }

    public BlurSession getBlurSession() {
        return blurSession;
    }
}
