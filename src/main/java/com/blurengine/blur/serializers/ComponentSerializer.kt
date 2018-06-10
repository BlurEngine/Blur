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

package com.blurengine.blur.serializers

import com.supaham.commons.bukkit.text.TextParsers
import net.kyori.text.Component
import pluginbase.config.serializers.Serializer
import pluginbase.config.serializers.SerializerSet

class ComponentSerializer: Serializer<Component> {
    override fun deserialize(serialized: Any?, wantedType: Class<*>, serializerSet: SerializerSet): Component? {
        if (serialized == null) return null
        if (serialized !is String) throw IllegalArgumentException("Expecting string, got ${serialized.javaClass.typeName}")
        return TextParsers.XML.parse(serialized)
    }

    override fun serialize(`object`: Component?, serializerSet: SerializerSet): Any? {
        throw UnsupportedOperationException()
    }
}

