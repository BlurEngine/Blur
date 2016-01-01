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

package com.blurengine.blur.modules.stages;

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.modules.framework.Module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Stage of a {@link ModuleManager}, typically representing a {@link BlurSession} stage. Meant for games that provide multiple stages.
 */
public final class Stage {

    private final StageManager manager;
    private final String name;
    private final List<Module> modules;

    public Stage(StageManager manager, String name, List<Module> modules) {
        this.manager = Preconditions.checkNotNull(manager, "manager cannot be null.");
        this.name = Preconditions.checkNotNull(name, "name cannot be null.");
        this.modules = Preconditions.checkNotNull(modules, "modules cannot be null.");
    }

    public StageManager getManager() {
        return manager;
    }

    public String getName() {
        return name;
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(this.modules);
    }
}
