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

package com.blurengine.blur.modules;

import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.BoundariesModule.BoundariesData;
import com.blurengine.blur.framework.ModuleData;

@ModuleInfo(name = "Boundaries", dataClass = BoundariesData.class)
public class BoundariesModule extends Module {

    private final Extent extent;

    public BoundariesModule(ModuleManager moduleManager, Extent extent) {
        super(moduleManager);
        this.extent = extent;
    }

    // TODO kill the cowards!

    public static final class BoundariesData implements ModuleData {

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            Extent extent = moduleManager.getExtentManager().getOrCreateExtent(serialized.getAsObject());
            return new BoundariesModule(moduleManager, extent);
        }
    }
}
