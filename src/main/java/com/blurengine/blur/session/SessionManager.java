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

package com.blurengine.blur.session;

import com.google.common.collect.Iterables;

import com.blurengine.blur.Blur;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a {@link BlurSession} manager. Only one SessionManager instance should exist at any one time. 
 */
public class SessionManager {

    protected final Blur blur;
    private final Set<BlurSession> blurSessions = new HashSet<>();

    public SessionManager(Blur blur) {
        this.blur = blur;
    }

    public Blur getBlur() {
        return blur;
    }

    public Collection<BlurSession> getBlurSessions() {
        return Collections.unmodifiableCollection(blurSessions);
    }

    public BlurSession getFirstSession() {
        return this.blurSessions.isEmpty() ? null : Iterables.get(this.blurSessions, 0);
    }

    public BlurSession getLastSession() {
        return this.blurSessions.isEmpty() ? null : Iterables.get(this.blurSessions, this.blurSessions.size() - 1);
    }
}
