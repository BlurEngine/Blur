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

import com.supaham.commons.bukkit.utils.VectorUtils;
import com.supaham.commons.utils.RandomUtils;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Represents a Cuboid shaped {@link Extent}, containing two points where the cuboid begins and ends, minimum to maximum respectively.
 */
public class CuboidExtent implements Extent {

    private Vector min;
    private Vector max;

    public CuboidExtent(@Nonnull Vector v1, @Nonnull Vector v2) {
        Preconditions.checkNotNull(v1, "v1 cannot be null.");
        Preconditions.checkNotNull(v2, "v2 cannot be null.");
        this.min = Vector.getMinimum(v1, v2);
        this.max = Vector.getMaximum(v1, v2);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return x >= min.getBlockX() && x < max.getBlockX() + 1 &&
            y >= min.getBlockY() && y < max.getBlockY() + 1 &&
            z >= min.getBlockZ() && z < max.getBlockZ() + 1;
    }

    @Override
    public double getVolume() {
        return (max.getX() - min.getX()) * (max.getY() - min.getY()) * (max.getZ() - min.getZ());
    }

    @Override
    public Vector getRandomLocation(Random random) {
        double x = RandomUtils.nextDouble(min.getX(), max.getX());
        double y = RandomUtils.nextDouble(min.getY(), max.getY());
        double z = RandomUtils.nextDouble(min.getZ(), max.getZ());
        return new Vector(x, y, z);
    }

    @Override
    public Iterator<BlockVector> iterator() {
        return new CuboidIterator();
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    public Vector getMinimumPoint() {
        return min;
    }

    public Vector getMaximumPoint() {
        return max;
    }

    private class CuboidIterator implements Iterator<BlockVector> {

        private int nextX = min.getBlockX();
        private int nextY = min.getBlockY();
        private int nextZ = min.getBlockZ();

        @Override
        public boolean hasNext() {
            return (this.nextX != Integer.MIN_VALUE);
        }

        @Override
        public BlockVector next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BlockVector answer = new BlockVector(nextX, nextY, nextZ);
            if (++nextX > max.getBlockX()) {
                nextX = min.getBlockX();
                if (++nextY > max.getBlockY()) {
                    nextY = min.getBlockY();
                    if (++nextZ > max.getBlockZ()) {
                        nextX = Integer.MIN_VALUE;
                    }
                }
            }
            return answer;
        }
    }
}
