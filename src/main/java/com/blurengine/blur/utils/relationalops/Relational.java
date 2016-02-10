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

package com.blurengine.blur.utils.relationalops;

import com.blurengine.blur.utils.relationalops.Relationals.RelationalStaticOp;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;

/**
 * Represents a relational interface between two relatable objects that can be compared by the implementation of {@link #apply(Object, Object)}.
 */
public interface Relational<T> extends BiFunction<T, RelationalOperator, Boolean> {

    /**
     * Returns a new Relational that inverts the non-zero integer result this relational returns. Where result == 0, 0 is returned. Where result is 
     * {@code >0}, {@code -n} is returned. Where result is {@code <0}, {@code +n} is returned.
     *
     * @return new relational
     */
    default Relational<T> inversed() {
        return Relationals.inversed(this);
    }

    default Relational<T> staticOp(@Nonnull RelationalOperator operator) {
        return Relationals.staticOp(this, operator);
    }

    default boolean isStaticOp() {
        return this instanceof RelationalStaticOp;
    }
}
