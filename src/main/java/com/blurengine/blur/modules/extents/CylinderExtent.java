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

import java.util.Iterator;

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
    public Iterator<BlockVector> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
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
