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

package com.blurengine.blur.modules.stages

import java.util.Collections
import javax.annotation.Nonnull

class StageChangeData(val reason: StageChangeReason) {
    private val _customData = HashMap<Class<*>, Any>()
    /**
     * Unmodifiable collection
     */
    val customData: Collection<Any> = Collections.unmodifiableCollection(_customData.values)

    operator fun <T> get(dataClass: Class<T>) = _customData[dataClass] as? T

    @Nonnull
    fun <T> getOrCreate(dataClass: Class<T>): T {
        var data = get(dataClass)
        if (data == null) {
            data = dataClass.newInstance()!!
            put(data)
        }
        return data
    }

    inline fun <reified T : Any> get() = get(T::class.java)

    @Nonnull
    inline fun <reified T : Any> getOrCreate() = getOrCreate(T::class.java)

    fun put(data: Any) {
        require(data.javaClass !in _customData) { "${data.javaClass} is already registered." }
        _customData[data.javaClass] = data
    }

    fun remove(data: Any): Boolean {
        return _customData.remove(data.javaClass) == data
    }
}
