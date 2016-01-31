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

package com.blurengine.blur.modules.framework.serializer;

import com.blurengine.blur.modules.framework.Module;
import com.supaham.commons.serializers.ListSerializer;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

/**
 * Represents a List accepting serializer version of {@link ModuleSerializer}.
 */
public class ListModuleSerializer extends ListSerializer<Module> {

    @Override
    public Class<Module> getTypeClass() {
        return Module.class;
    }
}
