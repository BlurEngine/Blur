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

package com.blurengine.blur.framework;

/**
 * Represents the loading type of a {@link Module} to expand its capability.
 */
public enum ModuleLoadType {

    /**
     * Allow modules to load prior to the minecraft world being loaded. This is typically used to modify the world generation.
     */
    PRE_WORLD,
    /**
     * Allow the module to load only after the world is loaded.
     */
    POST_WORLD;
}
