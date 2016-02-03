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

package com.blurengine.blur.modules.filters;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.InternalModule;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.framework.ModuleManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a {@link Filter} manager. This manager contains all filters registered by a {@link ModuleManager} meaning these same filters can be 
 * reused in child {@link BlurSession}.
 */
@ModuleInfo(name = "BlurFilterManager")
@InternalModule
public class FilterManager extends Module {

    private Map<String, Filter> filters = new HashMap<>();

    static {
        ModuleLoader.register(FiltersModule.class);
    }

    public FilterManager(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public void addFilter(String id, Filter filter) {
        Preconditions.checkArgument(id == null || !this.filters.containsKey(id), "filter with id '%s' already exists.", id);
        if (id == null) {
            id = UUID.randomUUID().toString();
            while (this.filters.containsKey(id)) {
                id = UUID.randomUUID().toString();
            }
        }
        this.filters.put(id, filter);
    }

    /* ================================
     * >> GETTERS
     * ================================ */

    public Collection<Filter> getFilters() {
        return Collections.unmodifiableCollection(filters.values());
    }

    public Filter getFilterById(String id) {
        return this.filters.get(id);
    }
}
