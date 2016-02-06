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

import com.supaham.commons.utils.StringUtils;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an amendable object that consists of a Map of String->Object. Meant to provide a means of mapping data to known keys. This is important
 * in general to provide a compatible middle man for <b>Supervisor</b>.
 */
public interface Amendable {

    /**
     * Appends a {@code key}, alongside a formatted message with values.
     *
     * @param key key
     * @param message message
     * @param values values to format the message with
     */
    default void append(@Nonnull String key, @Nonnull String message, @Nullable Object... values) {
        StringUtils.checkNotNullOrEmpty(message, "message");
        Preconditions.checkNotNull(values, "values cannot be null.");
        append(key, values.length == 0 ? message : String.format(message, values));
    }

    /**
     * Appends a {@code key} with a {@code byte} value.
     *
     * @param key key
     * @param value byte value
     */
    default void append(@Nonnull String key, byte value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code short} value.
     *
     * @param key key
     * @param value short value
     */
    default void append(@Nonnull String key, short value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with an {@code int} value.
     *
     * @param key key
     * @param value int value
     */
    default void append(@Nonnull String key, int value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code long} value.
     *
     * @param key key
     * @param value long value
     */
    default void append(@Nonnull String key, long value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code float} value.
     *
     * @param key key
     * @param value float value
     */
    default void append(@Nonnull String key, float value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code double} value.
     *
     * @param key key
     * @param value double value
     */
    default void append(@Nonnull String key, double value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code boolean} value.
     *
     * @param key key
     * @param value boolean value
     */
    default void append(@Nonnull String key, boolean value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with a {@code char} value.
     *
     * @param key key
     * @param value char value
     */
    default void append(@Nonnull String key, char value) {
        append(key, String.valueOf(value));
    }

    /**
     * Appends a {@code key} with an {@link Object} value.
     *
     * @param key key
     * @param value value, nullable
     */
    void append(@Nonnull String key, @Nullable Object value);

    Map<String, Object> getEntries();
}
