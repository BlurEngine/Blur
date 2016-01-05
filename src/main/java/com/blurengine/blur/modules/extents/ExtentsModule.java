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

package com.blurengine.blur.modules.extents;

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.extents.ExtentsModule.ExtentsData;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializer;
import com.blurengine.blur.modules.extents.serializer.ExtentSerializer.ListExtentSerializer;
import com.blurengine.blur.serializers.ExtentList;

import java.util.List;

import pluginbase.config.annotation.SerializeWith;

/**
 * Represents a {@link Module} that allows for the creation of {@link Extent} and nothing else. Intended for user convenience.
 */
@ModuleInfo(name = "Extents", dataClass = ExtentsData.class)
public class ExtentsModule extends Module {

    private ExtentsModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public static final class ExtentsData implements ModuleData {

        private ExtentList extents;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof List) {
                ExtentSerializer ser = moduleManager.getModuleLoader().getExtentSerializer();
                serialized.getAsList().stream().map(map -> ser.deserialize(map, null))
                    .forEach((s) -> { // This module doesnt care about the deserialized extents, those are registered to the session.
                    });
            } else {
                serialized.load(this);
            }
            return new ExtentsModule(moduleManager);
        }
    }
}
