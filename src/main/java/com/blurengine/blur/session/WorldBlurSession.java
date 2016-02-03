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

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.ModuleManager;

import org.bukkit.World;

import javax.annotation.Nonnull;

/**
 * Represents a {@link BlurSession} extension that consists of a {@link World}. This is the most common type of {@link BlurSession} for the reason 
 * that a session is typically interacted with through a world.
 */
public class WorldBlurSession extends BlurSession {

    protected final World world;

    public WorldBlurSession(@Nonnull BlurSession parentSession, @Nonnull World world) {
        super(Preconditions.checkNotNull(parentSession, "parentSession cannot be null."), null);
        this.world = Preconditions.checkNotNull(world, "world cannot be null.");
    }

    private WorldBlurSession(@Nonnull BlurSession parentSession, @Nonnull World world, @Nonnull ModuleManager moduleManager) {
        super(Preconditions.checkNotNull(parentSession, "parentSession cannot be null."),
            Preconditions.checkNotNull(moduleManager, "moduleManager cannot be null."));
        this.world = Preconditions.checkNotNull(world, "world cannot be null.");
    }

    public WorldBlurSession createChildSession(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world cannot be null.");
        WorldBlurSession session = new WorldBlurSession(this, world, this.moduleManager);
        super.addChildSession(session);
        return session;
    }

    public World getWorld() {
        return world;
    }
}
