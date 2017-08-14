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

package com.blurengine.blur.serializers.thirdparty

import com.github.zafarkhaja.semver.Version
import pluginbase.config.serializers.Serializer
import pluginbase.config.serializers.SerializerSet

class VersionSerializer : Serializer<Version> {
    override fun serialize(`object`: Version?, serializerSet: SerializerSet): Any? {
        return `object`.toString()
    }

    override fun deserialize(serialized: Any?, wantedType: Class<*>, serializerSet: SerializerSet): Version? {
        if (serialized == null) return null
        require(serialized is String || serialized is Number) { "version must be a string" }
        var serialized = serialized
        if (serialized is Number) {
            serialized = "${serialized.toDouble()}.0"
        }
        return Version.valueOf(serialized.toString())
    }
}
