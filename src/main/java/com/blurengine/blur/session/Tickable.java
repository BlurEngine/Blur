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

package com.blurengine.blur.session;

import com.blurengine.blur.modules.RandomCompassTargetModule;

/**
 * Represents an object that can tick with the {@link BlurSession}.
 * <p />
 * The following is an example of of an implementation of {@link Tickable} taken from {@link RandomCompassTargetModule}:
 * <pre>
 *
 * public class MyTickable implements Tickable {
 *    {@literal @}Tick(interval = "30s")
 *     private void updateBait() {
 *         getAnyPlayer(Predicates.ALIVE).ifPresent(p -> {
 *             this.bait = p;
 *             broadcastMessage(new FancyMessage().safeAppend("&e" + this.bait.getName() + " got baited!"));
 *         });
 *     }
 * }
 * </pre>
 */
public interface Tickable {
}
