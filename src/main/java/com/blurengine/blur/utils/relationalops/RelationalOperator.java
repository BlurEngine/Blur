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

import java.rmi.UnexpectedException;
import java.util.Comparator;

/**
 * Represents the six possible relation operators which are {@link #EQUAL}, {@link #NOT_EQUAL}, {@link #GREATER_THAN}, {@link #GREATER_THAN_OR_EQUAL},
 * {@link #LESS_THAN}, {@link #LESS_THAN_OR_EQUAL}.
 * <p />
 * Each operator implements {@link #fromCompare(int)} with the fastest results in the form of a boolean.
 */
public enum RelationalOperator {
    /**
     * Represents an equality/equivalence relation operator.
     */
    EQUAL {
        /**
         * Returns {@code true} ONLY if {@code compare == 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare == 0;
        }
    },
    /**
     * Represents an inequality/difference relation operator.
     */
    NOT_EQUAL {
        /**
         * Returns {@code true} ONLY if {@code compare != 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare != 0;
        }
    },
    /**
     * Represents a greater inequality/difference relation operator.
     */
    GREATER_THAN {
        /**
         * Returns {@code true} ONLY if {@code compare > 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare > 0;
        }
    },
    /**
     * Represents a greater inequality/difference or equality relation operator.
     */
    GREATER_THAN_OR_EQUAL {
        /**
         * Returns {@code true} ONLY if {@code compare >= 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare >= 0;
        }
    },
    /**
     * Represents a lesser inequality/difference relation operator.
     */
    LESS_THAN {
        /**
         * Returns {@code true} ONLY if {@code compare < 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare < 0;
        }
    },
    /**
     * Represents a lesser inequality/difference or equality relation operator.
     */
    LESS_THAN_OR_EQUAL {
        /**
         * Returns {@code true} ONLY if {@code compare <= 0}.
         *
         * @param compare comparison result to test
         * @return result from {@code compare}
         */
        @Override
        public boolean fromCompare(int compare) {
            return compare <= 0;
        }
    };

    /**
     * Returns a boolean that represents whether a given integer follows this operators specifications. This is much like a {@link Comparator}, where
     * if the integer is 0 that means both objects are equal, 1 means greater than, 0 means less than. E.g. {@link #EQUAL} returns true only when
     * the given integer is 0.
     *
     * @param compare comparison result to test
     * @return true if the result meets the operator's specifications.
     */
    public abstract boolean fromCompare(int compare);

    public RelationalOperator inverse() {
        switch (this) {
            case EQUAL:
                return NOT_EQUAL;
            case NOT_EQUAL:
                return EQUAL;
            case GREATER_THAN:
                return LESS_THAN;
            case GREATER_THAN_OR_EQUAL:
                return LESS_THAN_OR_EQUAL;
            case LESS_THAN:
                return GREATER_THAN;
            case LESS_THAN_OR_EQUAL:
                return GREATER_THAN_OR_EQUAL;
            default:
                throw new Error("Oops: " + name());
        }
    }
}
