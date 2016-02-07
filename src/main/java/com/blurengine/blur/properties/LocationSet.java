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

package com.blurengine.blur.properties;

import com.blurengine.blur.properties.LocationSet.LocationSetPropertyHandler;
import com.supaham.commons.bukkit.utils.LocationUtils;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import pluginbase.config.annotation.HandlePropertyWith;
import pluginbase.config.annotation.IgnoreSuperFields;
import pluginbase.config.annotation.SerializableAs;
import pluginbase.config.field.FieldInstance;
import pluginbase.config.field.PropertyVetoException;
import pluginbase.config.properties.PropertyHandler;

/**
 * Represents a list of {@link Location}s meant to represent spawn locations.
 */
@SerializableAs("Spawns")
//@SerializeWith(LocationSetSerializer.class)
@HandlePropertyWith(LocationSetPropertyHandler.class)
@IgnoreSuperFields
public class LocationSet extends HashSet<Location> {

    private static double DISTANCE_THRESHOLD = 1d;
    private double distanceThreshold;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     *
     * @throws IllegalArgumentException if the specified initial capacity
     * is negative
     */
    public LocationSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public LocationSet() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     *
     * @throws NullPointerException if the specified collection is null
     */
    public LocationSet(Collection<Location> c) {
        super(c);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     *
     * @throws NullPointerException if the specified collection is null
     */
    public LocationSet(Collection<Location> c, double distanceThreshold) {
        super(c);
        this.distanceThreshold = distanceThreshold;
    }

    @Override
    public boolean add(Location location) {
        return fuzzySpawnMatch(location) != null && super.add(location);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Location) {
            Location location = fuzzySpawnMatch(((Location) o));
            return super.remove(location != null ? location : o);
        }
        return false;
    }

    /**
     * Tries to find a spawn point match of a {@link Location}.
     *
     * @param location location to match
     *
     * @return the matched {@link Location} if found, otherwise null
     */
    @Nullable
    private Location fuzzySpawnMatch(@Nonnull Location location) {
        for (Location spawn : this) {
            if (spawn.distance(location) <= (distanceThreshold > 0 ? distanceThreshold
                : DISTANCE_THRESHOLD)) {
                return spawn;
            }
        }
        return null;
    }

//  public static final class LocationSetSerializer implements Serializer<LocationSet> {
//
//    @Nullable @Override public Object serialize(LocationSet object, SerializerSet serializerSet)
//        throws IllegalArgumentException {
//      if (object == null) {
//        return null;
//      }
//      Object serialized = Serializers.getSerializer(ListLocationSerializer.class)
//          .serialize(new ArrayList<>(object));
//      // Only map if the distance has been set by the user
//      if (object.distanceThreshold > 0) {
//        Map<String, Object> map = new LinkedHashMap<>();
//        map.put("threshold", object.distanceThreshold);
//        map.put("list", serialized);
//        return map;
//      }
//      return serialized;
//    }
//
//    @Nullable @Override public LocationSet deserialize(@Nullable Object serialized, @Nonnull Class wantedType, SerializerSet serializerSet)
//        throws IllegalArgumentException {
//      if (serialized == null) {
//        return null;
//      }
//      if (serialized instanceof Map) {
//        Map<String, Object> map = ((Map) serialized);
//        double threshold = Double.parseDouble(map.get("threshold").toString());
//        List<Location> list = Serializers.getSerializer(ListLocationSerializer.class)
//            .deserialize(map.get("list"), List.class);
//        return new LocationSet(list, threshold);
//      }
//      return new LocationSet(Serializers.getSerializer(ListLocationSerializer.class)
//                                 .deserialize(serialized, List.class));
//    }
//  }

    public static final class LocationSetPropertyHandler implements PropertyHandler {

        @Override
        public void set(@Nonnull FieldInstance field, @Nonnull String newValue)
            throws PropertyVetoException, UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(@Nonnull FieldInstance field, @Nonnull String valueToAdd)
            throws PropertyVetoException, UnsupportedOperationException {
            ((List<Location>) field.getValue()).add(LocationUtils.deserialize(valueToAdd));
        }

        @Override
        public void remove(@Nonnull FieldInstance field, @Nonnull String valueToRemove)
            throws PropertyVetoException, UnsupportedOperationException {
            ((List<Location>) field.getValue()).remove(LocationUtils.deserialize(valueToRemove));
        }

        @Override
        public void clear(@Nonnull FieldInstance field, @Nullable String valueToClear)
            throws PropertyVetoException, UnsupportedOperationException {
            ((List<Location>) field.getValue()).clear();
        }
    }
}
