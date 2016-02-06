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

package com.blurengine.blur.supervisor;

import javax.annotation.Nonnull;

/**
 * Represents a compatible extension for linking with <b>Supervisor</b>. To use, simply implement this interface in your class, such as Module class,
 * and when appropriate Blur will invoke the {@link #run(Amendable)} when <b>Supervisor</b> requests information.
 * <p />
 * The purpose of this class is a middle man between Blur and Supervisor to ensure compatibility and optionality to make Modules function with or
 * without <b>Supervisor</b>.
 */
public interface SupervisorContext {

    void run(@Nonnull Amendable amendable);
}
