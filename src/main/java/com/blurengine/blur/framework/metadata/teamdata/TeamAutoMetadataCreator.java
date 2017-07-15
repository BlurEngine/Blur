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

package com.blurengine.blur.framework.metadata.teamdata;

import com.blurengine.blur.framework.Component;
import com.blurengine.blur.framework.metadata.auto.AbstractAutoMetadataCreator;
import com.blurengine.blur.framework.metadata.auto.MetadataCreator;
import com.blurengine.blur.modules.teams.BlurTeam;
import com.blurengine.blur.session.BlurSession;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

public class TeamAutoMetadataCreator extends AbstractAutoMetadataCreator<BlurTeam> {

    private final Component ownerComponent;

    public TeamAutoMetadataCreator(Component ownerComponent) {
        this.ownerComponent = ownerComponent;
    }

    @Nonnull
    @Override
    protected Object instantiateClass(@Nonnull Class<?> clazz, @Nonnull BlurTeam blurTeam) {
        // Automatic zero-arg/one-arg constructor
        try {
            return clazz.getDeclaredConstructor(BlurTeam.class).newInstance(blurTeam);
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
     * Registers a class as a Team data class. Team data classes are classes that are instantiated automatically when a team is being adding to
     * a {@link BlurSession}. In order for this feature to function properly, the given {@link Class} <b>MUST</b> have one of the following:
     * <ul>
     *     <li>A publicly accessible zero-arg constructor</li>
     *     <li>A publicly accessible one-arg constructor of type {@link BlurTeam}</li>
     * </ul>
     * Failure to do so will cause valid errors. <p />
     *
     * For more control over class instantiation see {@link #registerClass(Class, MetadataCreator)}
     *
     * @param clazz Team data class
     * @see #registerClass(Class, MetadataCreator)
     */
    @Override
    public <T> void registerClass(@Nonnull Class<T> clazz) {
        super.registerClass(clazz);
        for (BlurTeam blurTeam : this.ownerComponent.getTeamManager().getTeams()) {
            Object data = instantiateClass(clazz, blurTeam);
            this.ownerComponent.getTeamManager().addTeamData(this.ownerComponent, blurTeam, data);
        }
    }

    /**
     * Registers a Team data class instance creator alongside its class (given). 
     * @param clazz Team data class that is supplied from the {@code creator}
     * @param creator creator of Team Data class instance
     * @param <T> type of class being supplied
     */
    @Override
    public <T> void registerClass(@Nonnull Class<T> clazz, @Nonnull MetadataCreator<T, BlurTeam> creator) {
        super.registerClass(clazz, creator);
        for (BlurTeam blurTeam : this.ownerComponent.getTeamManager().getTeams()) {
            T data = creator.create(blurTeam);
            this.ownerComponent.getTeamManager().addTeamData(this.ownerComponent, blurTeam, data);
        }
    }
    // TODO removeTeamData on unregister
}
