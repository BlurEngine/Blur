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
import com.google.common.collect.Maps;

import com.blurengine.blur.framework.AbstractComponent;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.modules.filters.Filter.FilterResponse;
import com.blurengine.blur.modules.goal.GoalModule.ScoreGoalData;
import com.blurengine.blur.modules.stages.StageChangeReason;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreComponent extends AbstractComponent {

    private final Map<Object, Score> scores = new HashMap<>();
    private final List<ScoreGoalData> goals;
    private final Map<Object, ScoreGoalData> scorersGoals = new HashMap<>();

    public ScoreComponent(@Nonnull ModuleManager moduleManager, @Nullable List<ScoreGoalData> data) {
        super(moduleManager);
        this.goals = data == null ? Collections.emptyList() : data;
    }

    public boolean checkForStageChange(@Nonnull Score score) {
        Preconditions.checkNotNull(score, "score cannot be null.");

        Optional<StageChangeReason> stageChangeReason = score.checkGoalMet();
        stageChangeReason.ifPresent(r -> {
            if (r == StageChangeReason.OBJECTIVE_SUCCESS) {
                getSession().callEvent(new GoalWinnersEvent(getSession(), Collections.singleton(score.getObject())));
            }
            getStageManager().nextStage(r);
        });
        return stageChangeReason.isPresent();
    }

    /**
     * Returns an immutable map of objects with a score represented by a {@code double} value.
     *
     * @return immutable map of object -> score
     */
    @Nonnull
    public Map<Object, Double> getScores() {
        return Collections.unmodifiableMap(Maps.transformValues(scores, Score::getScore));
    }

    /**
     * Returns an {@link OptionalDouble} which may be populated only if this {@link ScoreComponent} registers the given Object's score.
     *
     * @param scorer object that may hold a score
     * @return OptionalDouble
     */
    @Nonnull
    public OptionalDouble getScore(@Nonnull Object scorer) {
        return Optional.ofNullable(this.scores.get(scorer))
            .map(score -> OptionalDouble.of(score.getScore()))
            .orElse(OptionalDouble.empty());
    }

    /**
     * Adds a given score for a given object. If the given scorer does not have an existing score, one is created by default, then the given score 
     * is added onto them.
     *
     * @param scorer object that may hold a score
     * @param score score to add
     * @return {@code scorer}'s new score
     */
    public double addScore(@Nonnull Object scorer, double score) {
        Score scoreObj = getOrCreate(scorer);
        double newScore = scoreObj.add(score);
        checkForStageChange(scoreObj);
        return newScore;
    }

    /**
     * Deducts a given score from a given object. If the given scorer does not have an existing score, one is created by default, then the given score 
     * is deducted from them.
     *
     * @param scorer object that may hold a score
     * @param score score to deduct
     * @return {@code scorer}'s new score
     */
    public double deductScore(@Nonnull Object scorer, double score) {
        Score scoreObj = getOrCreate(scorer);
        double newScore = scoreObj.subtract(score);
        checkForStageChange(scoreObj);
        return newScore;
    }

    /**
     * Resets a scorer's score.
     *
     * @param scorer object that may hold a score
     */
    public void resetScore(@Nonnull Object scorer) {
        // Don't call getOrCreate as it is unnecessary for resetting an already null score.
        Preconditions.checkNotNull(scorer, "scorer cannot be null.");
        Optional.ofNullable(this.scores.get(scorer)).ifPresent(Score::reset);
    }

    private Score getOrCreate(@Nonnull Object scorer) {
        Preconditions.checkNotNull(scorer, "scorer cannot be null.");
        Score foundScore = this.scores.get(scorer);
        if (foundScore == null) {
            ScoreGoalData data = getGoalDataFor(scorer);
            this.scores.put(scorer, foundScore = new Score(scorer, data));
        }
        return foundScore;
    }

    private ScoreGoalData getGoalDataFor(@Nonnull Object object) {
        return goals.stream().filter(gd -> gd.getFilter() == null || gd.getFilter().test(object) == FilterResponse.ALLOW).findFirst()
            .orElse(NullScoreGoalData.INSTANCE); // Return NullScoreGoalData as a safety precaution to not require goals to be set to keep score.
    }
}
