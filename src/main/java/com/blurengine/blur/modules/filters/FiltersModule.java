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

import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.filters.FiltersModule.FiltersData;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.modules.filters.serializer.FilterSerializer;

import java.util.List;

import pluginbase.config.annotation.SerializeWith;

/**
 * Represents a {@link Module} that allows for the creation of {@link Filter} and nothing else. Intended for user convenience.
 */
@ModuleInfo(name = "Filters", dataClass = FiltersData.class)
public class FiltersModule extends Module {

    private FiltersModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public static final class FiltersData implements ModuleData {

        @SerializeWith(FilterSerializer.class)
        private List<Filter> filters;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new FiltersModule(moduleManager);
        }
    }
}
