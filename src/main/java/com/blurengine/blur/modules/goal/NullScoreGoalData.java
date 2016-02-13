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

import com.blurengine.blur.modules.goal.GoalModule.ScoreGoalCase;
import com.blurengine.blur.modules.goal.GoalModule.ScoreGoalData;
import com.blurengine.blur.modules.stages.StageChangeReason;
import com.blurengine.blur.utils.relationalops.Relationals;

import java.util.Arrays;

/**
 * Represents a null safe instance of {@link ScoreGoalData} that uses {@link Double#MAX_VALUE} as the goal.
 */
public class NullScoreGoalData extends ScoreGoalData {

    public static final NullScoreGoalData INSTANCE = new NullScoreGoalData();

    private NullScoreGoalData() {
        super(null, 0, Arrays.asList(new ScoreGoalCase(StageChangeReason.OBJECTIVE_SUCCESS, Double.MAX_VALUE, Relationals.number(Double.MAX_VALUE))));
    }
}
