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

package com.blurengine.blur.utils

inline fun <T, R : Comparable<R>> Iterable<T>.allMaxBy(selector: (T) -> R): List<T> {
    val iterator = iterator()
    if (!iterator.hasNext()) return listOf()
    val maxElems = mutableListOf(iterator.next())
    var maxValue = selector(maxElems.first())
    while (iterator.hasNext()) {
        val next = iterator.next()
        val value = selector(next)
        inner@for (maxElem in maxElems) {
            if (value >= maxValue) {
                if (value > maxValue) { // new max value
                    maxElems.clear()
                    maxValue = value
                }
                maxElems.add(next)
                break@inner
            }
        }
    }
    return maxElems
}
