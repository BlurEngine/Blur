/*
 * Copyright 2018 Ali Moghnieh
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

package com.blurengine.blur.utils

import org.junit.Assert
import org.junit.Test

class CollectionsTests {
    @Test
    fun AllMinSingle() {
        val collection = listOf(1, 4, 2, 4, 3, 2)
        val minCollection = collection.allMin()
        Assert.assertEquals(minCollection, listOf(1))
    }

    @Test
    fun AllMaxSingle() {
        val collection = listOf(1, 4, 2, 3, 1, 2)
        val minCollection = collection.allMax()
        Assert.assertEquals(minCollection, listOf(4))
    }

    @Test
    fun AllMinMultiple() {
        val collection = listOf(1, 4, 2, 4, 3, 1, 2)
        val minCollection = collection.allMin()
        Assert.assertEquals(minCollection, listOf(1, 1))
    }

    @Test
    fun AllMaxMultiple() {
        val collection = listOf(1, 4, 2, 4, 3, 1, 2)
        val minCollection = collection.allMax()
        Assert.assertEquals(minCollection, listOf(4, 4))
    }
}
