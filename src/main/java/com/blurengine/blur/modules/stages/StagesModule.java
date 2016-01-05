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

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.serializers.ModuleList;
import com.blurengine.blur.modules.stages.StagesModule.StagesData;

import java.util.ArrayList;
import java.util.List;

import pluginbase.config.annotation.NoTypeKey;

@ModuleInfo(name = "Stages", dataClass = StagesData.class)
public class StagesModule extends Module {

    static {
        
    }

    public StagesModule(ModuleManager moduleManager, List<StageData> stages) throws ModuleParseException {
        super(moduleManager);
        StageManager mgr = moduleManager.getStageManager();
        stages.stream().map(data -> new Stage(mgr, data.name, data.modules)).forEach(mgr::addStage);
    }

    public static final class StagesData implements ModuleData {

        private final List<StageData> stageDatas = new ArrayList<>(0);

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new StagesModule(moduleManager, stageDatas);
        }
    }

    @NoTypeKey
    private static final class StageData {

        private String name;
        private ModuleList modules;
    }
}
