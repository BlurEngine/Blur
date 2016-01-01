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

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ModuleParseException;
import com.blurengine.blur.modules.framework.SerializedModule;
import com.blurengine.blur.modules.MapInfoModule.MapInfoData;
import com.blurengine.blur.modules.framework.ModuleData;
import com.supaham.commons.bukkit.serializers.ColorStringSerializer;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.NoTypeKey;
import pluginbase.config.annotation.SerializeWith;

@ModuleInfo(name = "MapInfo", dataClass = MapInfoData.class)
public class MapInfoModule extends Module {

    private final MapInfoData data;

    public MapInfoModule(@NonNull ModuleManager moduleManager, MapInfoData data) {
        super(moduleManager);
        this.data = data;
    }

    public MapInfoData getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MapInfoModule{" + data.toString() + "}";
    }

    @Getter
    @ToString
    public static final class MapInfoData implements ModuleData {

        @Name("blur-version")
        private String blurVersion;
        private String version;
        private String id;
        @SerializeWith(ColorStringSerializer.class)
        private String name;
        @SerializeWith(ColorStringSerializer.class)
        private String description;
        private List<Author> authors;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new MapInfoModule(moduleManager, this);
        }
    }

    @NoTypeKey
    @Getter
    @ToString
    public static final class Author {

        private UUID uuid;
        private String role;
    }
}
