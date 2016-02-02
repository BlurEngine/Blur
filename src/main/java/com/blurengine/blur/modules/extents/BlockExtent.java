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

import com.google.common.base.Preconditions;

import com.supaham.commons.bukkit.utils.ImmutableBlockVector;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Represents a Cuboid shaped {@link Extent}, containing two points where the cuboid begins and ends, minimum to maximum respectively.
 */
public class BlockExtent implements Extent {

    public static final BlockExtent ZERO = new BlockExtent(new ImmutableBlockVector(0, 0, 0));

    private ImmutableBlockVector vector;

    public BlockExtent(@Nonnull ImmutableBlockVector vector) {
        this.vector = Preconditions.checkNotNull(vector, "vector cannot be null.");
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return x >= vector.getBlockX() && x < vector.getBlockX() + 1 &&
            y >= vector.getBlockY() && y < vector.getBlockY() + 1 &&
            z >= vector.getBlockZ() && z < vector.getBlockZ() + 1;
    }

    @Override
    public double getVolume() {
        return 1;
    }

    @Override
    public Vector getRandomLocation(Random random) {
        return vector.toBlockVector().add(new Vector(0.5, 0, 0.5));
    }

    @Override
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            boolean done;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public BlockVector next() {
                if (this.done) {
                    throw new NoSuchElementException();
                }
                this.done = true;
                return BlockExtent.this.vector.toBlockVector();
            }
        };
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    public ImmutableBlockVector getVector() {
        return vector;
    }
}
