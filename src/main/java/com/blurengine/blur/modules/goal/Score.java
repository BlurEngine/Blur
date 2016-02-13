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

import com.blurengine.blur.modules.goal.GoalModule.ScoreGoalCase;
import com.blurengine.blur.modules.goal.GoalModule.ScoreGoalData;
import com.blurengine.blur.modules.stages.StageChangeReason;
import com.blurengine.blur.utils.relationalops.RelationalOperator;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Represents a wrapper for an Object that can have a score.
 * <p />
 * This class is package-private as it is mutable and is meant only for trusted access.
 */
class Score {

    private final Object object;
    private final ScoreGoalData goalData;

    private double score;

    public Score(@Nonnull Object object, @Nonnull ScoreGoalData goalData) {
        this.object = Preconditions.checkNotNull(object, "object cannot be null.");
        this.goalData = Preconditions.checkNotNull(goalData, "goalData cannot be null.");
    }

    public Optional<StageChangeReason> checkGoalMet() {
        for (ScoreGoalCase _case : this.goalData.getCases()) {
            if (_case.getGoalRelational().apply(getScore(), RelationalOperator.EQUAL)) {
                return Optional.of(_case.getResult());
            }
        }
        return Optional.empty();
    }

    public double reset() {
        return this.score = getInitial();
    }

    public void set(double score) {
        this.score = score;
    }

    public double add(double score) {
        return this.score += score;
    }

    public double subtract(double score) {
        return this.score -= score;
    }

    public double increment() {
        return ++this.score;
    }

    public double decrement() {
        return --this.score;
    }

    public double getScore() {
        return score;
    }

    public Object getObject() {
        return object;
    }

    public double getInitial() {
        return this.goalData.getInitialScore();
    }
}
