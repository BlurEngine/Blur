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

package com.blurengine.blur.modules.framework;

/**
 * This is an empty interface meant to identify classes that are created by a {@link Module} to store configurable data.
 */
public interface ModuleData {

    Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException;

    default <T> T checkNotNull(T object, String message) throws ModuleParseException {
        if (object == null) {
            throw new ModuleParseException(message);
        }
        return object;
    }

    default <T> T checkNotNull(T object, String message, Object... args) throws ModuleParseException {
        if (object == null) {
            throw new ModuleParseException(String.format(message, args));
        }
        return object;
    }

    default void check(boolean b, String message) throws ModuleParseException {
        if (!b) {
            throw new ModuleParseException(message);
        }
    }

    default void check(boolean b, String message, Object... args) throws ModuleParseException {
        if (!b) {
            throw new ModuleParseException(String.format(message, args));
        }
    }
}
