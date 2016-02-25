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

package com.blurengine.blur.modules.filters.serializer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.FilterManager;
import com.blurengine.blur.modules.filters.lexer.FilterRecursiveDescentParser;
import com.blurengine.blur.modules.filters.serializer.FilterSerializers.Material;
import com.supaham.commons.bukkit.utils.SerializationUtils;
import com.supaham.commons.utils.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.serializers.SerializerSet;

public class FilterSerializer implements BlurSerializer<Filter> {

    private static final String[] RESERVED_SERIALIZERS = new String[]{"team"};
    private static final Map<String, Class<? extends FilterTypeSerializer>> FILTER_SERIALIZERS = new HashMap<>();

    private final ModuleLoader moduleLoader;
    private final Map<String, FilterTypeSerializer<?>> serializers;
    private final Function<String, Filter> filterGetter = s -> getManager().getFilterById(s);

    static {
        FILTER_SERIALIZERS.put("material", Material.class);
    }

    /**
     * Registers a clazz for a {@link FilterSerializer} to use when deserializing. The {@code filterType} is the name of the filter type, e.g.
     * union, that will be handled by the given clazz.
     *
     * @param filterType name of region type that is handled by the given {@code clazz}
     * @param clazz clazz to handle the {@code filterType}
     *
     * @return previous clazz that was removed by this execution
     */
    public static Class<? extends FilterTypeSerializer> registerSerializer(String filterType, Class<? extends FilterTypeSerializer> clazz) {
        Preconditions.checkNotNull(filterType, "filterType cannot be null.");
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        filterType = filterType.toLowerCase();
        Preconditions.checkArgument(ArrayUtils.contains(RESERVED_SERIALIZERS, filterType), "%s is a reserved filter type.", filterType);
        return FILTER_SERIALIZERS.put(filterType, clazz);
    }

    public FilterSerializer(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;

        Map<String, FilterTypeSerializer<?>> serializers = new HashMap<>();
        FILTER_SERIALIZERS.forEach((k, v) -> {
            try {
                serializers.put(k, v.getDeclaredConstructor(FilterSerializer.class).newInstance(this));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        });
        this.serializers = ImmutableMap.copyOf(serializers);
    }

    @Override
    public Filter deserialize(@Nullable Object serialized, @Nonnull Class wantedType, @Nonnull SerializerSet serializerSet) {
        if (serialized == null) {
            return null;
        } else if (serialized instanceof Map) {
            return deserializeMapToFilter((Map<String, Object>) serialized);
        } else if (serialized instanceof String) {
            String str = serialized.toString().trim();
            Preconditions.checkArgument(str.isEmpty(), "Filter String is empty.");
            // Not ID reference. This is a definition.
            Filter found = null;
            // If the string doesn't have a whitespace, it's most likely an id reference
            if (!str.matches("\\s+")) {
                found = filterGetter.apply(str);
            }
            // If the string has spaces or the id reference was null, build a filter from the string.
            if (found == null) {
                found = deserializeStringToFilter(str);
            }
            Preconditions.checkNotNull(found, "Invalid reference id or definition: %s", str);
            return found;
        }
        throw new IllegalArgumentException("Expected List or Map, got data type of " + serialized.getClass().getName() + ".");
    }

    public Filter deserializeStringToFilter(String string) {
        return deserializeStringToFilter(null, string);
    }

    public Filter deserializeStringToFilter(String id, String string) {
        getManager().checkNonExistant(id);
        Filter filter = new FilterRecursiveDescentParser(this.filterGetter, string).call();
        getManager().addFilter(id, filter);
        return filter;
    }

    public Filter deserializeMapToFilter(Map<String, Object> map) {
        Preconditions.checkArgument(!map.isEmpty(), "given map is empty.");

        if (map.size() == 1) {
            return deserializeSingleMapEntryToFilter(map);
        }

        List<String> invalidTypes = new ArrayList<>();
        Optional<String> id = map.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("id")).findFirst().map(e -> e.getValue().toString());

        Filter filter = null;
        for (Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equalsIgnoreCase("id")) {
                continue;
            }
            // Get serializer by the name of the given key.
            FilterTypeSerializer<?> serializer = serializers.get(e.getKey());
            if (serializer == null) {
                invalidTypes.add(e.getKey());
            } else {
                filter = serializer.deserialize(e.getValue(), null, SerializationUtils.SERIALIZER_SET);
            }
        }

        // Notify the user of defined invalid extent types.
        if (!invalidTypes.isEmpty()) {
            moduleLoader.getLogger().warning("Unknown extent types: " + invalidTypes);
        }

        // If no filter was defined, then we must have an id reference
        if (filter == null) {
            String filterId = id.orElseThrow(() -> new NullPointerException("no filter id or filter definition."));
            filter = getManager().getFilterById(filterId);
            Preconditions.checkNotNull(filter, "Could not find filter by id '%s'.", id);
        } else {
            getManager().addFilter(id.orElse(null), filter);
        }

        return filter;
    }

    public Filter deserializeSingleMapEntryToFilter(Map<String, Object> map) {
        Entry<String, Object> entry = map.entrySet().iterator().next();
        String key = entry.getKey();
        Object value = entry.getValue();

        // Simple {id:"id"} map.
        if (key.equalsIgnoreCase("id")) {
            Preconditions.checkArgument(value instanceof String, "map of id expects String value, got: %s", value);
            return Preconditions.checkNotNull(getManager().getFilterById(value.toString()), "Could not find filter by id '%s'.", value);
        }

        Filter filter = null;
        String id = null;
        // If key starts with !, it's an explicit filter id
        if (!key.startsWith("!")) {
            FilterTypeSerializer<?> ser = this.serializers.get(key);
            if (ser != null) {
                filter = ser.deserialize(value, Filter.class, moduleLoader.getSerializerSet());
            }
        }
        if (filter == null) {
            id = (key.startsWith("!") ? key.substring(1) : key).trim();
            Preconditions.checkArgument(!id.isEmpty(), "A filter id is empty with the data: %s", value);
            Preconditions.checkArgument(value instanceof Map, "Filter value of '%s' must be Map. Got type %s", id, value.getClass().getName());
            filter = deserializeMapToFilter((Map<String, Object>) value);
        }
        getManager().addFilter(id, filter);
        return filter;
    }

    public FilterManager getManager() {
        return this.moduleLoader.getModuleManager().getFilterManager();
    }
}
