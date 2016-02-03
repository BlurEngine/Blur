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

package com.blurengine.blur.framework.ticking;

/**
 * Represents a mutable integer supplier interface. This is used in conjunction with {@link TickField} to create automatically ticking fields for
 * convenience and organisability.
 */
public interface BAutoInt {

    /**
     * Returns the integer wrapped in this object.
     * 
     * @return integer
     */
    int get();

    /**
     * Sets this integer to {@code n}.
     *
     * @param n integer to set this integer to
     * @return the given {@code n}
     */
    default int set(int n) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds {@code n} to this integer.
     *
     * @param n integer to add to this integer
     * @return the previous value plus the given {@code n}
     */
    default int add(int n) {
        throw new UnsupportedOperationException();
    }

    /**
     * Subtracts {@code n} from this integer.
     *
     * @param n integer to subtract from this integer
     * @return the previous value minus the given {@code n}
     */
    default int subtract(int n) {
        throw new UnsupportedOperationException();
    }
}
