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

package com.blurengine.blur.inventory;

/**
 * Represents a type of slot that may be present in an {@link InventoryLayout}.
 */
public interface SlotType {

    SlotType EMPTY = new SlotType() {
        @Override
        public String getName() {
            return "EMPTY";
        }

        @Override
        public boolean isClickable() {
            return true;
        }
    };

    String getName();

    /**
     * Returns whether this slot type is clickable when a human entity clicks on it when their inventory is open.
     *
     * @return whether this slot type is clickable
     */
    default boolean isClickable() {
        return false;
    }
}
