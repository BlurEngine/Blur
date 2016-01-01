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

package com.blurengine.blur.modules.filters;

/**
 * Represents a {@link Filter} that is used to represent a condition to control tasks.
 */
public interface Filter {

    FilterResponse test(Object object);

    default Filter inverse() {
        return Filters.inverse(this);
    }

    default Filter and(Filter other) {
        return (object) -> {
            FilterResponse first = test(object);
            FilterResponse second = other.test(object);
            if (first == FilterResponse.ABSTAIN) {
                return second;
            } else if (second == FilterResponse.ABSTAIN) {
                return first;
            } else {
                return first.isAllowed() && second.isAllowed() ? FilterResponse.ALLOW : FilterResponse.DENY;
            }
        };
    }

    default Filter or(Filter other) {
        return (object) -> {
            FilterResponse first = test(object);
            FilterResponse second = other.test(object);
            if (first == FilterResponse.ABSTAIN) {
                return second;
            } else if (second == FilterResponse.ABSTAIN) {
                return first;
            } else {
                return first.isAllowed() || second.isAllowed() ? FilterResponse.ALLOW : FilterResponse.DENY;
            }
        };
    }

    enum FilterResponse {
        ALLOW,
        DENY,
        ABSTAIN;

        public static FilterResponse from(boolean allow) {
            return allow ? ALLOW : DENY;
        }

        public FilterResponse inverse() {
            return this == ALLOW ? DENY : this == DENY ? ALLOW : ABSTAIN;
        }

        public boolean isAllowed() {
            return this == ALLOW || this == ABSTAIN;
        }

        public boolean isDenied() {
            return this == DENY;
        }
    }
}
