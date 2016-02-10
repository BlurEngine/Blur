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

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * Represents a {@link Relational} helper class that provides implementations that compare certain types against each other. Some of the handy 
 * features in here also include {@link #inversed(Relational)} and {@link #staticOp(Relational, RelationalOperator)} which are used to modify output
 * but while still obeying their original {@link Relational}.
 */
public class Relationals {

    /**
     * Creates and return a new {@link Relational} that always uses the same {@link RelationalOperator}, which is given in this method.
     *
     * @param relational relational to provide static operator for
     * @param operator static operator to always provide
     * @param <T> type of relational
     * @return new relational with static operator
     */
    public static <T> Relational<T> staticOp(@Nonnull Relational<T> relational, RelationalOperator operator) {
        // Prevent redundant nested statics and just create a new branch. 
        if (relational.isStaticOp()) {
            return new RelationalStaticOp<>(((RelationalStaticOp) relational).original, operator);
        } else {
            return new RelationalStaticOp<>(relational, operator);
        }
    }

    /**
     * Returns a new {@link Relational} that inverts the non-zero integer result the given {@link Relational} returns. Where result == 0, 0 is 
     * returned. Where result is {@code >0}, {@code -n} is returned. Where result is {@code <0}, {@code +n} is returned.
     * <p />
     * If the given is already inversed, the original relational is returned preventing nested inverting.
     *
     * @param relational relational to inverse
     * @param <T> type of relational being inverted
     * @return new relational
     */
    public static <T> Relational<T> inversed(@Nonnull Relational<T> relational) {
        Preconditions.checkNotNull(relational, "relational cannot be null.");
        if (relational instanceof InversedRelational) {
            return ((InversedRelational) relational).original;
        } else {
            return new InversedRelational<>(relational);
        }
    }

    /**
     * Creates and returns a new {@link Relational} that handles numbers. The Relational provided does its check by converting both numbers into
     * {@link Double} and comparing them against each other using {@link Double#compare(double, double)}. The first parameter is the provided 
     * {@link Number} at the time of the request, and the second parameter is this given {@code number}. This ensures that the specifications are
     * followed based on {@link RelationalOperator} and its values.
     *
     * @param number number to relate to in the test
     * @return new relational for numbers
     */
    public static Relational<Number> number(@Nonnull Number number) {
        return new NumberRelational(Preconditions.checkNotNull(number, "number cannot be null."));
    }

    /* ================================
     * >> MODIFIERS
     * ================================ */

    static final class RelationalStaticOp<T> implements Relational<T> {

        private final Relational<T> original;
        private final RelationalOperator operator;

        public RelationalStaticOp(Relational<T> original, RelationalOperator operator) {
            this.original = original;
            this.operator = operator;
        }

        @Override
        public Boolean apply(T t, RelationalOperator operator) {
            return this.original.apply(t, this.operator);
        }
    }

    private static final class InversedRelational<T> implements Relational<T> {

        private final Relational<T> original;

        public InversedRelational(Relational<T> original) {
            this.original = original;
        }

        @Override
        public Boolean apply(T t, RelationalOperator operator) {
            return this.original.apply(t, operator.inverse());
        }
    }
    
    /* ================================
     * >> GENERIC JAVA TYPES
     * ================================ */

    private static final class NumberRelational implements Relational<Number> {

        private final double d;

        public NumberRelational(Number number) {
            this.d = number.doubleValue();
        }

        @Override
        public Boolean apply(Number number, RelationalOperator operator) {
            return operator.fromCompare(Double.compare(number.doubleValue(), this.d));
        }
    }
}
