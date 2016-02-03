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

package com.blurengine.blur.framework.serializer;

import com.blurengine.blur.framework.Module;

/**
 * Represents a {@link RuntimeException} that is thrown when a {@link Module} could not be found by the given name, accessible through 
 * {@link #getName()}.
 */
public class ModuleNotFoundException extends RuntimeException {

    private String name;

    public ModuleNotFoundException(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
