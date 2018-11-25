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

import com.supaham.commons.bukkit.utils.ImmutableVector;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Represents a Cylinder {@link Extent} implementation where a circular area is selected with a height.
 */
public class CylinderExtent implements Extent {

    private final ImmutableVector base;
    private final double radius;
    private final double height;

    public CylinderExtent(@Nonnull ImmutableVector base, double radius, double height) {
        this.base = Preconditions.checkNotNull(base, "base vector cannot be null.");
        this.radius = radius;
        this.height = height;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        if (y < base.getY() || (y > base.getY() + height)) {
            return false;
        }
        return Math.pow(x - base.getX(), 2) + Math.pow(z - base.getZ(), 2) < radius * radius;
    }

    @Override
    public double getVolume() {
        return Math.PI * radius * radius * height;
    }

    @Override
    public Vector getRandomLocation(Random random) {
        double x = base.getX();
        double z = base.getZ();

        double rAngle = random.nextDouble() * 360;
        double rLength = Math.pow(random.nextDouble(), 0.5) * radius; // sqrt double to evenly distribute the random location.
        double rX = x + rLength * Math.cos(rAngle);
        double rZ = z + rLength * Math.sin(rAngle);
        return new Vector(rX, base.getY() + random.nextDouble() * height, rZ);
    }

    @Override
    public Iterator<BlockVector> iterator() {
        int spacing = 20;
        List<BlockVector> result = new ArrayList<>(spacing);
        double PI2 = Math.PI * 2.0;
        for (int i = 0; i <= spacing; i++) {
            double angle = ((double) i / spacing) * PI2;
            double dX = Math.cos(angle) * this.radius + this.base.getX();
            double dZ = Math.sin(angle) * this.radius + this.base.getZ();

            // TODO Height 1 currently counts two separate heights, should it stay like that?
            for (int j = 0; j <= (int) this.height; j++) {
                result.add(new BlockVector(dX, this.base.getY() + j, dZ));

            }
        }
        return result.iterator();
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    public ImmutableVector getBase() {
        return base;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }
}
