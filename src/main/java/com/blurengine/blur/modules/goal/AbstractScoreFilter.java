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

import com.blurengine.blur.modules.filters.Filter;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Represents an abstract {@link Filter} implementation for handling {@link ScoreComponent}.
 */
public abstract class AbstractScoreFilter implements Filter {

    private final GoalModule goalModule;
    private Map<Object, Double> winnersCache;

    public AbstractScoreFilter(@Nonnull GoalModule goalModule) {
        this.goalModule = Preconditions.checkNotNull(goalModule, "goalModule cannot be null.");
    }

    protected abstract Map<Object, Double> getWinners(Map<Object, Double> scorers);

    @Override
    public FilterResponse test(Object object) {
        if (object instanceof Score) {
            Map<Object, Double> winners = this.winnersCache;
            if (winners == null) {
                winners = this.winnersCache = getWinners(goalModule.getScores());
            }
            return FilterResponse.from(winners.containsKey(object));
        }
        return FilterResponse.ABSTAIN;
    }

    public GoalModule getGoalModule() {
        return goalModule;
    }
}
