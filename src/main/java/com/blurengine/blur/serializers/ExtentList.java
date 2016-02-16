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

package com.blurengine.blur.serializers;

import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.serializers.ExtentList.ExtentListSerializer;
import com.supaham.commons.collections.PossiblyImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import pluginbase.config.annotation.SerializeWith;
import pluginbase.config.serializers.SerializerSet;

/**
 * Represents a {@link List} of {@link Extent} specifically designed for easy serialization.
 */
@SerializeWith(ExtentListSerializer.class)
public class ExtentList extends PossiblyImmutableList<Extent> {

    public static final ExtentList EMPTY = new ExtentList();

    public ExtentList() {
        super();
    }

    public ExtentList(int initialCapacity) {
        super(new ArrayList<>(initialCapacity));
    }

    public ExtentList(Collection<? extends Extent> c) {
        super(new ArrayList<>(c));
    }

    public static final class ExtentListSerializer implements BlurSerializer<ExtentList> {

        @Override
        public ExtentList deserialize(Object serialized, Class wantedType, SerializerSet serializerSet) throws IllegalArgumentException {
            if (serialized == null) {
                return EMPTY;
            }
            pluginbase.config.serializers.Serializer<Extent> ser = serializerSet.getClassSerializer(Extent.class);
            if (serialized instanceof List) {
                return new ExtentList(((List<Object>) serialized).stream().map(o -> ser.deserialize(o, Extent.class, serializerSet)).collect(Collectors.toList()));
            } else {
                return new ExtentList(Collections.singletonList(ser.deserialize(serialized, Extent.class, serializerSet)));
            }
        }
    }
}
