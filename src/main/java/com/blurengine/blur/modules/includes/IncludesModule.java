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

package com.blurengine.blur.modules.includes;

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.framework.serializer.ListModuleSerializer;
import com.blurengine.blur.modules.framework.serializer.ModuleList;
import com.blurengine.blur.modules.includes.IncludesModule.IncludesData;
import com.supaham.commons.Joiner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pluginbase.config.annotation.SerializeWith;

@ModuleInfo(name = "Includes", dataClass = IncludesData.class)
public class IncludesModule extends Module {

    public IncludesModule(ModuleManager moduleManager, List<Module> includes) {
        super(moduleManager);
        includes.forEach(this::addSubmodule);
    }

    public static final class IncludesData implements ModuleData {

        private List<String> includes;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof List) {
                this.includes = (List<String>) serialized.getAsList();
            } else if (serialized.getAsObject() instanceof String) {
                this.includes = Collections.singletonList(serialized.getAsString());
            }
            List<Module> includes = new ArrayList<>();

            List<File> _inexistant = new ArrayList<>();
            List<File> _nonFiles = new ArrayList<>();
            for (String include : this.includes) {
                File file = new File(moduleManager.getSession().getRootDirectory(), include);
                moduleManager.getLogger().fine("Checking " + file.getAbsolutePath());
                if (!file.exists()) {
                    _inexistant.add(file);
                } else if (!file.isFile()) {
                    _nonFiles.add(file);
                } else {
                    IncludeFileData includeData = new IncludeFileData();
                    moduleManager.getModuleLoader().deserializeYAMLFileTo(file, includeData);
                    // Configurate treats present empty lists as null.
                    if (includeData.modules != null) {
                        includes.addAll(includeData.modules);
                    }
                }
            }
            if (_inexistant.size() > 0) {
                moduleManager.getLogger().severe("The following files do not exist: " + Joiner.on(", ").join(_inexistant));
            }
            if (_nonFiles.size() > 0) {
                moduleManager.getLogger().severe("The following must be files: " + Joiner.on(", ").join(_nonFiles));
            }
            return new IncludesModule(moduleManager, includes);
        }
    }

    public static final class IncludeFileData {

        private ModuleList modules;
    }
}
