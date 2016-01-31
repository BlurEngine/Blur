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

import com.blurengine.blur.modules.framework.ticking.TickFieldHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that an internal class is extending Module as a means of convenience and organisability. A case where internal modules are registered
 * alongside other modules is {@link TickFieldHolder}. The reason being that the class uses a task, and instead of having to start/stop the task, the
 * ModuleManager is already designed for and capable of doing such tasks. This eliminates the need for creating those extra boilerplate methods and
 * clutter the ModuleManager with their usages.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface InternalModule {
}
