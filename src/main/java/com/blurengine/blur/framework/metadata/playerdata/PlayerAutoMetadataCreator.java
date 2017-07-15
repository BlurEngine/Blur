/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.framework.metadata.playerdata;

import com.blurengine.blur.framework.metadata.auto.AbstractAutoMetadataCreator;
import com.blurengine.blur.framework.metadata.auto.MetadataCreator;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

public class PlayerAutoMetadataCreator extends AbstractAutoMetadataCreator<BlurPlayer> {

    @Nonnull
    @Override
    protected Object instantiateClass(@Nonnull Class<?> clazz, @Nonnull BlurPlayer blurPlayer) {
        // Automatic zero-arg/one-arg constructor
        try {
            return clazz.getDeclaredConstructor(BlurPlayer.class).newInstance(blurPlayer);
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e1) {
                throw new RuntimeException(e1);
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a class as a Player Data class. Player Data classes are classes that are instantiated automatically when a player is being adding to
     * a {@link BlurSession}. In order for this feature to function properly, the given {@link Class} <b>MUST</b> have one of the following:
     * <ul>
     *     <li>A publicly accessible zero-arg constructor</li>
     *     <li>A publicly accessible one-arg constructor of type {@link BlurPlayer}</li>
     * </ul>
     * Failure to do so will cause valid errors. <p />
     *
     * For more control over class instantiation see {@link #registerClass(Class, MetadataCreator)}
     *
     * @param clazz Player Data class
     * @see #registerClass(Class, MetadataCreator)
     */
    @Override
    public <T> void registerClass(@Nonnull Class<T> clazz) {
        super.registerClass(clazz);
    }

    /**
     * Registers a Player Data class instance creator alongside its class (given). 
     * @param clazz Player Data class that is supplied from the {@code creator}
     * @param creator creator of Player Data class instance
     * @param <T> type of class being supplied
     */
    @Override
    public <T> void registerClass(@Nonnull Class<T> clazz, @Nonnull MetadataCreator<T, BlurPlayer> creator) {
        super.registerClass(clazz, creator);
    }
}
