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

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

/**
 * Represents an {@link Extent} that consists of {@link Vector}s, radius, and offset in radians.
 */
public class AutoCircleExtent implements Extent {

    protected Vector base;
    protected List<Vector> pointsList;
    protected double radius;
    protected double offsetRadians;

    protected int lastPoint;
    protected List<BlockVector> bvCache;

    public AutoCircleExtent(@Nonnull Vector base, @Nonnull Collection<Vector> pointsList, double radius, double offsetRadians) {
        Preconditions.checkNotNull(base, "base cannot be null.");
        Preconditions.checkNotNull(pointsList, "points cannot be null.");
        Preconditions.checkArgument(radius > 0, "radius must be greater than 0.");

        this.base = base;
        if (pointsList instanceof List) {
            this.pointsList = Collections.unmodifiableList((List<Vector>) pointsList);
        } else {
            this.pointsList = Collections.unmodifiableList(new ArrayList<>(pointsList));
        }
        this.radius = radius;
        this.offsetRadians = offsetRadians;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        Vector test = new Vector(x, y, z);
        return pointsList.stream().filter(v -> VectorUtils.isSameBlock(test, v)).count() > 0;
    }

    @Override
    public double getVolume() {
        return pointsList.size();
    }

    @Override
    public MutableExtent mutable() throws UnsupportedOperationException {
        MutableAutoCircleExtent mutable = new MutableAutoCircleExtent(this.base, this.radius, this.pointsList.size(), this.offsetRadians);
        mutable.pointsList = new ArrayList<>(this.pointsList);
        return mutable;
    }

    /**
     * Returns the next point in sequential order of this method's calls. After the last point has been returned, the next one will be the first.
     *
     * @param random Random instance, nullable
     * @return next Vector in sequence
     */
    @Override
    public Vector getRandomLocation(Random random) {
        Vector vector = this.pointsList.get(this.lastPoint);
        if (++this.lastPoint >= this.pointsList.size()) {
            this.lastPoint = 0;
        }
        return vector;
    }

    @Override
    public Iterator<BlockVector> iterator() {
        if (this.bvCache == null) {
            this.bvCache = Collections.unmodifiableList(this.pointsList.stream().map(Vector::toBlockVector).collect(Collectors.toList()));
        }
        return this.bvCache.iterator();
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    public List<Vector> getPointsList() {
        return pointsList;
    }

    public double getRadius() {
        return radius;
    }

    public int getPoints() {
        return this.pointsList.size();
    }

    public double getOffsetRadians() {
        return offsetRadians;
    }

    public static final class MutableAutoCircleExtent extends AutoCircleExtent implements MutableExtent {

        private int points;
        private boolean dirty = true;

        public MutableAutoCircleExtent(@Nonnull Vector base, double radius, int points, double offsetRadians) {
            super(base, new ArrayList<>(), radius, offsetRadians);
            setPoints(points);
            regenerate();
        }

        public void setBase(@Nonnull Vector base) {
            if (!this.base.equals(base)) {
                this.base = Preconditions.checkNotNull(base, "base cannot be null.");
                this.dirty = true;
            }
        }

        public void setPoints(int points) {
            if (this.points != points) {
                Preconditions.checkArgument(points > 0, "points cannot be less than 1.");
                this.points = points;
                this.dirty = true;
            }
        }

        public void setOffsetRadians(double offsetRadians) {
            if (this.offsetRadians != offsetRadians) {
                this.offsetRadians = offsetRadians;
                this.dirty = true;
            }
        }

        public void setRadius(double radius) {
            if (this.radius != radius) {
                Preconditions.checkArgument(radius > 0, "radius must be greater than 0.");
                this.radius = radius;
                this.dirty = true;
            }
        }

        public void regenerate() {
            if (!dirty) {
                return;
            }
            double x = base.getX();
            double z = base.getZ();
            // TODO optimize by using already existing vectors.
            this.pointsList = IntStream.range(0, points).mapToObj(i -> {
                double angle = ((double) i / points) * Math.PI * 2 + offsetRadians;
                double dX = Math.cos(angle) * radius + x;
                double dZ = Math.sin(angle) * radius + z;
                return new Vector(dX, base.getY(), dZ);
            }).collect(Collectors.toList());
        }

        @Override
        public MutableExtent mutable() {
            return this;
        }
    }
}
