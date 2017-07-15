/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.framework.metadata.teamdata;

import com.blurengine.blur.session.BlurSession;

/**
 * Interface for more control over custom Team data Classes. See {@link TeamAutoMetadataCreator}.
 */
public interface TeamData {

    /**
     * Called after this instance is created and registered into the {@link BlurSession}. This is <b>NOT</b> equivalent to implementing the instance
     * initialization block. The difference is this method is called after registration to the {@link BlurSession}. 
     */
    default void enable() {}

    /**
     * Called before this instance is unregistered from the {@link BlurSession}.
     */
    default void disable() {}
}
