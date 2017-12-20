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

package com.blurengine.blur.modules.extents.serializer

import com.blurengine.blur.framework.BlurSerializer
import com.blurengine.blur.framework.ModuleLoader
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.extents.ExtentManager
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.AutoCircle
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Block
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Cuboid
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Cylinder
import com.blurengine.blur.modules.extents.serializer.ExtentSerializers.Union
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import com.supaham.commons.bukkit.utils.SerializationUtils
import com.supaham.commons.utils.ArrayUtils
import pluginbase.config.serializers.SerializerSet
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import java.util.HashMap

/**
 * Represents an [Extent] serializer. Keep in mind this is for single extents. For a [List] of extents, see [ExtentList].
 */
class ExtentSerializer(private val moduleLoader: ModuleLoader) : BlurSerializer<Extent> {
    private val serializers: Map<String, ExtentTypeSerializer<*>>

    val manager: ExtentManager
        get() = this.moduleLoader.moduleManager.extentManager

    init {
        val serializers = HashMap<String, ExtentTypeSerializer<*>>()
        EXTENT_SERIALIZERS.forEach { k, v ->
            try {
                serializers.put(k, v.getDeclaredConstructor(ExtentSerializer::class.java).newInstance(this))
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }
        this.serializers = ImmutableMap.copyOf(serializers)
    }

    override fun deserialize(serialized: Any?, wantedType: Class<*>, serializerSet: SerializerSet): Extent? {
        return when (serialized) {
            null -> null
            is Map<*, *> -> deserializeExtent(serialized as Map<String, Any>)
            is String -> deserializeExtent(ImmutableMap.of<String, Any>("id", serialized.toString()))
            else -> throw IllegalArgumentException("Expected List or Map, got data type of " + serialized.javaClass.name + ".")
        }
    }

    /**
     * The given `map` must either include the key *id*, otherwise a registered serializer for any of the given key.
     *
     * @param map map of serialized extent
     *
     * @return the created or retrieved [Extent] instance
     */
    fun deserializeExtent(map: Map<String, Any>): Extent {
        val invalidTypes = ArrayList<String>()

        val id = map.entries.stream().filter { e -> e.key.equals("id", ignoreCase = true) }.findFirst().map { e -> e.value.toString() }

        var extent: Extent? = null
        for ((key, value) in map) {
            if (key.equals("id", ignoreCase = true)) {
                continue
            }
            // Get serializer by the name of the given key.
            val serializer = serializers[key]
            if (serializer == null) {
                invalidTypes.add(key)
            } else {
                extent = serializer.deserialize(value, Extent::class.java, SerializationUtils.SERIALIZER_SET)
            }
        }

        // Notify the user of defined invalid extent types.
        if (!invalidTypes.isEmpty()) {
            moduleLoader.logger.warning("Unknown extent types: " + invalidTypes)
        }

        // If no extent was defined, then we must have an id reference
        if (extent == null) {
            val extentId = id.orElseThrow { NullPointerException("no extent id or extent definition.") }
            extent = manager.getExtentByString(extentId)
        } else { // Extent was defined, add it.
            val idStr = id.orElse(null)
            manager.addExtent(idStr, extent)
        }
        return extent
    }

    companion object {
        private val RESERVED_SERIALIZERS = arrayOf("cuboid", "union", "cylinder", "block")
        private val EXTENT_SERIALIZERS = HashMap<String, Class<out ExtentTypeSerializer<*>>>()

        init {
            EXTENT_SERIALIZERS.put("cuboid", Cuboid::class.java)
            EXTENT_SERIALIZERS.put("union", Union::class.java)
            EXTENT_SERIALIZERS.put("cylinder", Cylinder::class.java)
            EXTENT_SERIALIZERS.put("block", Block::class.java)
            EXTENT_SERIALIZERS.put("auto-circle", AutoCircle::class.java)
        }

        /**
         * Registers a clazz for a [ExtentSerializer] to use when deserializing. The `extentType` is the name of the extent type, e.g.
         * union, that will be handled by the given clazz.
         *
         * @param extentType name of region type that is handled by the given `clazz`
         * @param clazz clazz to handle the `extentType`
         *
         * @return previous clazz that was removed by this execution
         */
        fun registerSerializer(extentType: String, clazz: Class<out ExtentTypeSerializer<*>>): Class<out ExtentTypeSerializer<*>>? {
            var extentType = extentType.toLowerCase()
            require(ArrayUtils.contains(RESERVED_SERIALIZERS, extentType)) { "$extentType is a reserved extent type." }
            return EXTENT_SERIALIZERS.put(extentType, clazz)
        }
    }
}
