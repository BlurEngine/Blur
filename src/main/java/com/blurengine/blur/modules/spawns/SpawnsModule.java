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

package com.blurengine.blur.modules.spawns;

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.spawns.serializer.SpawnSerializer.ListSpawnSerializer;
import com.blurengine.blur.modules.framework.ModuleData;
import com.blurengine.blur.modules.spawns.SpawnsModule.SpawnsData;

import java.util.ArrayList;
import java.util.List;

import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.SerializeWith;

@ModuleInfo(name = "Spawns", dataClass = SpawnsData.class)
public class SpawnsModule extends Module {

    private final Spawn defaultSpawn;
    private final List<Spawn> spawns;

    public SpawnsModule(ModuleManager moduleManager, SpawnsData data) {
        super(moduleManager);
        this.defaultSpawn = data.defaultSpawn;
        this.spawns = data.spawns;
    }

    public static final class SpawnsData implements ModuleData {

        @Name("default")
        private Spawn defaultSpawn = Spawn.ZERO;
        private SpawnList spawns = new SpawnList();

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);

            checkNotNull(defaultSpawn, "default cannot be null.");
            checkNotNull(spawns, "spawns cannot be null.");
            return new SpawnsModule(moduleManager, this);
        }
    }
}
