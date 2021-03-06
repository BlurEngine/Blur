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

import com.google.common.base.Preconditions;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Ali on 06/02/2016.
 */
public class SimpleAmendable implements Amendable {

    Map<String, Object> entries = new LinkedHashMap<>();

    @Override
    public void append(@Nonnull String key, @Nullable Object value) {
        Preconditions.checkNotNull(key, "key cannot be null.");
        this.entries.put(key, value);
    }

    @Override
    public Map<String, Object> getEntries() {
        return this.entries;
    }
}
