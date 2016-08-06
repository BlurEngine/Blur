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

package com.blurengine.blur.modules.extents;

import java.util.regex.Pattern;

/**
 * Thrown when an extent is not found by a {@link ExtentManager}.
 */
public class ExtentNotFoundException extends RuntimeException {

    public ExtentNotFoundException(String id) {
        super("Could not find extent by id '" + id + "'.");
    }

    public ExtentNotFoundException(Pattern pattern) {
        super("Could not find extent with pattern: " + pattern.pattern());
    }
}
