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

package com.blurengine.blur.modules.spawns;

import com.supaham.commons.bukkit.utils.ImmutableVector;
import com.supaham.commons.bukkit.utils.RelativeVector;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Represents a {@link Spawn} direction.
 */
public interface SpawnDirection {

    void applyTo(Location location, Entity entity);

    enum NullSpawnDirection implements SpawnDirection {
        INSTANCE;

        @Override
        public void applyTo(Location location, Entity entity) {}
    }

    /**
     * Represents a fixed {@link SpawnDirection} implementation.
     */
    final class FixedSpawnDirection implements SpawnDirection {

        private final float yaw, pitch;

        public FixedSpawnDirection(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public void applyTo(Location location, Entity entity) {
            location.setYaw(yaw);
            location.setPitch(pitch);
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }
    }

    /**
     * Represents a dynamic {@link SpawnDirection} implementation where the {@link Location} yaw and pitch get realigned to point towards the given
     * vector.
     */
    final class PointToSpawnDirection implements SpawnDirection {

        private final ImmutableVector vector;

        public PointToSpawnDirection(ImmutableVector vector) {
            this.vector = vector;
        }

        @Override
        public void applyTo(Location location, Entity entity) {
            ImmutableVector target = vector;
            if (vector instanceof RelativeVector) {
                if (entity instanceof LivingEntity) {
                    target = ((RelativeVector) vector).with(((LivingEntity) entity).getEyeLocation().toVector());
                } else {
                    target = ((RelativeVector) vector).with(location.toVector());
                }
            }

            // 
            Vector target2 = location.toVector().subtract(target.toVector());
            float angle = (float) Math.atan2(target2.getZ(), target2.getX());
            angle += Math.PI / 2.0; // add quarter of circle
            angle = (float) Math.toDegrees(angle);
            if (angle < 0) {
                angle += 360;
            }

            Vector playerHead = location.toVector().add(new Vector(0, ((LivingEntity) entity).getEyeHeight(), 0));
            Location playerOffset = target.subtract(playerHead).toLocation(location.getWorld());
            float pitch = (float) -Math.toDegrees(Math.asin(playerOffset.getY() / playerOffset.length()));
            location.setYaw(angle);
            location.setPitch(pitch);
        }

        public ImmutableVector getVector() {
            return vector;
        }
    }
}
