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

import com.blurengine.blur.modules.extents.ExtentDirection.NullExtentDirection;
import com.supaham.commons.bukkit.utils.ImmutableVector;
import com.supaham.commons.bukkit.utils.VectorUtils;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an {@link Extent} that consists of {@link Vector}s, radius, and offset in radians.
 */
public class AutoCircleExtent implements Extent, DirectionalExtent {

    protected Vector base;
    protected int points;
    protected double radius;
    protected double offsetRadians;
    protected ExtentDirection direction = NullExtentDirection.INSTANCE;

    protected List<Vector> pointsList;
    protected int lastPoint;
    protected List<BlockVector> bvCache;

    public AutoCircleExtent(@Nonnull Vector base, int points, double radius, double offsetRadians) {
        this(base, points, radius, offsetRadians, null);
    }

    public AutoCircleExtent(@Nonnull Vector base, int points, double radius, double offsetRadians,
                            @Nullable ExtentDirection direction) {
        Preconditions.checkNotNull(base, "base cannot be null.");
        Preconditions.checkArgument(points > 0, "points must be greater than 0.");
        Preconditions.checkArgument(radius > 0, "radius must be greater than 0.");

        this.base = base;
        this.radius = radius;
        this.offsetRadians = offsetRadians;
        this.pointsList = generatePoints();
        if (direction != null) {
            this.direction = direction;
        }
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
        MutableAutoCircleExtent mutable = new MutableAutoCircleExtent(this.base, this.radius, this.points, this.offsetRadians);
        mutable.pointsList = this.pointsList;
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

    @NotNull
    @Override
    public ExtentDirection getDirection() {
        return direction;
    }

    public ImmutableVector getBase() {
        return new ImmutableVector(this.base);
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

    protected List<Vector> generatePoints() {
        double x = base.getX();
        double z = base.getZ();
        // TODO optimize by using already existing vectors.
        List<Vector> points = IntStream.range(0, this.points).mapToObj(i -> {
            double angle = ((double) i / this.points) * Math.PI * 2 + offsetRadians;
            double dX = Math.cos(angle) * radius + x;
            double dZ = Math.sin(angle) * radius + z;
            return new Vector(dX, base.getY(), dZ);
        }).collect(Collectors.toList());
        return Collections.unmodifiableList(points);
    }

    public static final class MutableAutoCircleExtent extends AutoCircleExtent implements MutableExtent {

        private boolean dirty = false;

        public MutableAutoCircleExtent(@Nonnull Vector base, double radius, int points, double offsetRadians) {
            super(base, points, radius, offsetRadians);
            setPoints(points);
            regenerate();
        }

        public Vector getMutableBase() {
            return this.base;
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

        public void setDirection(@Nullable ExtentDirection direction) {
            if (direction == null) {
                direction = NullExtentDirection.INSTANCE;
            }
            if (!this.direction.equals(direction)) {
                this.direction = direction;
                this.dirty = true;
            }
        }

        public void regenerate() {
            if (dirty) {
                this.pointsList = generatePoints();
            }
        }

        @Override
        public MutableExtent mutable() {
            return this;
        }
    }
}
