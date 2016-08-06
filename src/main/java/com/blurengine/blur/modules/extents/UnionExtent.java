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

package com.blurengine.blur.modules.extents;

import com.supaham.commons.utils.CollectionUtils;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Represents a Union of {@link Extent}s which helps for grouping multiple Extents in one. 
 */
public class UnionExtent implements Extent {

    private Collection<Extent> extents;

    public UnionExtent(Extent... extents) {
        this(Arrays.asList(extents));
    }

    public UnionExtent(Collection<Extent> extents) {
        this.extents = Collections.unmodifiableCollection(extents);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        for (Extent extent : extents) {
            if (extent.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getVolume() {
        return -1; // TODO write code.
    }

    @Override
    public Vector getRandomLocation(Random random) {
        return CollectionUtils.getRandomElement(extents).getRandomLocation(random);
    }

    /*
         * Possibly bad practice but next() depends on hasNext() being called.
         */
    @Override
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            private final Iterator<Extent> extentIterator;
            private Iterator<BlockVector> curIt;

            {
                extentIterator = extents.iterator();
            }

            @Override
            public boolean hasNext() {
                // Nullify current iterator if it complete.
                if (curIt != null && !curIt.hasNext()) {
                    curIt = null;
                }

                if (curIt == null) {
                    if (!extentIterator.hasNext()) {
                        return false;
                    }
                    curIt = extentIterator.next().iterator();
                }
                return curIt.hasNext();
            }

            @Override
            public BlockVector next() {
                BlockVector next = curIt.next();
                if (next == null) {
                    throw new NoSuchElementException();
                }
                return next;
            }
        };
    }

    /**
     * Returns an immutable collection of {@link Extent}s that this Union consists of.
     * 
     * @return immutable collection of extents
     */
    public Collection<Extent> getExtents() {
        return Collections.unmodifiableCollection(extents);
    }
}
