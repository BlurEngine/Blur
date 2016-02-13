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

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents {@link AbstractScoreFilter} implementations.
 */
public class ScoreFilters {

    public static class LowestScoreFilter extends AbstractScoreFilter {

        public LowestScoreFilter(@Nonnull GoalModule goalModule) {
            super(goalModule);
        }

        @Override
        protected Map<Object, Double> getWinners(Map<Object, Double> scores) {
            double highestScore = scores.values().stream().mapToDouble(i -> i).min().orElse(-1);
            return scores.entrySet().stream().filter(e -> e.getValue() == highestScore)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
    }

    public static class HighestScoreFilter extends AbstractScoreFilter {

        public HighestScoreFilter(@Nonnull GoalModule goalModule) {
            super(goalModule);
        }

        @Override
        protected Map<Object, Double> getWinners(Map<Object, Double> scores) {
            double highestScore = scores.values().stream().mapToDouble(i -> i).max().orElse(-1);
            return scores.entrySet().stream().filter(e -> e.getValue() == highestScore)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
    }
}
