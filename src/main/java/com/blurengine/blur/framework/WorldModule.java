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

package com.blurengine.blur.framework;

import com.google.common.base.Preconditions;

import com.blurengine.blur.session.WorldBlurSession;

import org.bukkit.World;

/**
 * World Module framework base class. Upon extending this class, you will need to annotate the extension with {@link ModuleInfo} to tell the framework
 * how to load this module from the config file. Immediately after implementation, ensure that the class is also registered to the
 * {@link ModuleManager} using {@link ModuleLoader#register(Class)}.
 *
 * @see Module
 */
public class WorldModule extends Module {

    public WorldModule(ModuleManager moduleManager) {
        super(moduleManager);
        Preconditions.checkState(super.getSession() instanceof WorldBlurSession, "session is not WorldBlurSession.");
    }

    @Override
    public WorldBlurSession getSession() {
        return (WorldBlurSession) super.getSession();
    }

    public World getWorld() {
        return getSession().getWorld();
    }
}
