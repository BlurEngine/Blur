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

package com.blurengine.blur.modules.goal;

import com.google.common.base.Preconditions;

import com.blurengine.blur.events.session.BlurSessionEvent;
import com.blurengine.blur.session.BlurSession;

import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents an event where a {@link Winner} has completed a goal.
 */
public class GoalWinnersEvent extends BlurSessionEvent {

    private final List<Winner> winners;

    public GoalWinnersEvent(@Nonnull BlurSession blurSession, @Nonnull Collection<Winner> winners) {
        super(blurSession);
        Preconditions.checkNotNull(winners, "winner cannot be null.");
        this.winners = new ArrayList<>(winners);
    }

    /**
     * Returns an immutable {@link List} of the winners included in this event.
     * 
     * @return immutable list
     */
    @Nonnull
    public List<Winner> getWinners() {
        return Collections.unmodifiableList(this.winners);
    }

    public boolean addWinner(@Nonnull Winner winner) {
        return this.winners.add(winner);
    }

    public boolean removeWinner(@Nonnull Winner winner) {
        return this.winners.remove(winner);
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() { return handlerList; }

    public static HandlerList getHandlerList() { return handlerList; }
}
