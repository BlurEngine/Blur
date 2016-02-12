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

import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.includes.IncludesModule.IncludesData;
import com.blurengine.blur.serializers.ModuleList;
import com.supaham.commons.Joiner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ModuleInfo(name = "Includes", dataClass = IncludesData.class)
public class IncludesModule extends Module {

    public IncludesModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public static final class IncludesData implements ModuleData {

        private List<String> includes;

        /*
         * This piece of work includes external files into the current session. It is important to note that this actually does nothing to the
         * IncludesModule other than create a new instance. The actual inclusion of the module is done by the deserialization process by
         * calling deserializeYAMLFileTo, which then uses ModuleList and that includes it in the session.
         */
        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof List) {
                this.includes = serialized.getAsList();
            } else if (serialized.getAsObject() instanceof String) {
                this.includes = Collections.singletonList(serialized.getAsString());
            }

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
                    if (includeData.modules == null || includeData.modules.isEmpty()) {
                        moduleManager.getLogger().warning("Empty includes.");
                    } else {
                        List<String> collect = includeData.modules.stream().map(m -> m.getModuleInfo().name()).collect(Collectors.toList());
                        moduleManager.getLogger().fine("Added %d includes.", collect.size());
                        moduleManager.getLogger().finer("Includes: %s", Joiner.on(", ").join(collect));
                    }
                }
            }
            if (_inexistant.size() > 0) {
                moduleManager.getLogger().severe("The following files do not exist: " + Joiner.on(", ").join(_inexistant));
            }
            if (_nonFiles.size() > 0) {
                moduleManager.getLogger().severe("The following must be files: " + Joiner.on(", ").join(_nonFiles));
            }
            return new IncludesModule(moduleManager);
        }
    }

    public static final class IncludeFileData {

        private ModuleList modules;
    }
}
