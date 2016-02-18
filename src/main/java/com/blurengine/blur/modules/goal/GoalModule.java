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

import com.blurengine.blur.events.players.PlayerDeathEvent;
import com.blurengine.blur.framework.BlurSerializer;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.serializer.FilterSerializer;
import com.blurengine.blur.modules.goal.GoalModule.GoalModuleData;
import com.blurengine.blur.modules.stages.StageChangeReason;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.supervisor.Amendable;
import com.blurengine.blur.supervisor.SupervisorContext;
import com.blurengine.blur.utils.relationalops.Relational;
import com.blurengine.blur.utils.relationalops.RelationalUtils;
import com.blurengine.blur.utils.relationalops.Relationals;
import com.supaham.commons.utils.DurationUtils;
import com.supaham.commons.utils.MapBuilder;

import org.bukkit.event.EventHandler;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import javaslang.control.Match;
import pluginbase.config.annotation.Name;
import pluginbase.config.annotation.SerializeWith;
import pluginbase.config.serializers.SerializerSet;

@ModuleInfo(name = "Goal", dataClass = GoalModuleData.class)
public class GoalModule extends Module implements SupervisorContext {

    private final GoalModuleData data;
    private final ScoreComponent scoring;

    private Map<BlurPlayer, Integer> deaths = new HashMap<>();

    public GoalModule(ModuleManager moduleManager, GoalModuleData data) {
        super(moduleManager);
        this.data = data;
        this.scoring = new ScoreComponent(moduleManager, data.score);

        newTask(this::timeIsUp).delay(this.data.timeLimit).build();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.data.lives > 0) {
            BlurPlayer bp = event.getBlurPlayer();
            Integer deaths = this.deaths.getOrDefault(bp, 0);
            this.deaths.put(bp, ++deaths);
            if (deaths >= this.data.lives) {
                getModuleManager().getTeamManager().getSpectatorTeam().addPlayer(bp);
            }
        }
    }

    private void timeIsUp() {
        getLogger().fine("GameModule time limit of " + DurationUtils.toString(this.data.timeLimit, true) + " reached.");
        getStagesManager().nextStage(StageChangeReason.TIME_LIMIT);
    }

    public Map<Object, Double> getScores() {
        return scoring.getScores();
    }

    public double addScore(@Nonnull Object scorer, double score) {
        return scoring.addScore(scorer, score);
    }

    public double subtractScore(@Nonnull Object scorer, double score) {
        return scoring.deductScore(scorer, score);
    }

    public OptionalDouble getScore(@Nonnull Object scorer) {
        return scoring.getScore(scorer);
    }

    public void resetScore(@Nonnull Object scorer) {
        scoring.resetScore(scorer);
    }

    public GoalModuleData getData() {
        return data;
    }

    @Override
    public void run(@Nonnull Amendable amendable) {
        HashMap<Object, Object> config = new HashMap<>();
        config.put("time_limit", data.timeLimit);
        config.put("lives", data.lives);
        config.put("default-winner", data.defaultWinner);

        config.put("scores", this.scoring.getScores());
        config.put("score_goals", data.score.stream().map(g -> MapBuilder.newHashMap().put("initial-score", g.initialScore).put("filter", g.filter)
            .put("cases", g.cases).build()).collect(Collectors.toList()));

        amendable.append("config", config);

        List<Object> list = new ArrayList<>(this.deaths.size());
        this.deaths.entrySet().stream().forEach(e -> new SimpleEntry(e.getKey().getName(), e.getValue()));
        amendable.append("player_deaths", list);
    }

    public static final class GoalModuleData implements ModuleData {

        @Name("time-limit")
        private Duration timeLimit;
        private int lives;

        @Name("default-winner")
        private Filter defaultWinner;
        @SerializeWith(ScoreGoalDeserializer.class)
        private List<ScoreGoalData> score = new ArrayList<>();

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new GoalModule(moduleManager, this);
        }

        public Duration getTimeLimit() {
            return timeLimit;
        }

        public int getLives() {
            return lives;
        }

        public Filter getDefaultWinner() {
            return defaultWinner;
        }

        public List<ScoreGoalData> getScore() {
            return Collections.unmodifiableList(score);
        }
    }

    /* Not handled by PB, handled in ScoreGoalDeserializer. */
    public static class ScoreGoalData {

        private Filter filter;
        private double initialScore;
        private List<ScoreGoalCase> cases = new ArrayList<>();

        private ScoreGoalData() {
        }

        public ScoreGoalData(Filter filter, double initialScore, List<ScoreGoalCase> cases) {
            this.filter = filter;
            this.initialScore = initialScore;
            this.cases = cases;
        }

        public Filter getFilter() {
            return filter;
        }

        public double getInitialScore() {
            return initialScore;
        }

        public List<ScoreGoalCase> getCases() {
            return Collections.unmodifiableList(cases);
        }
    }

    /* Not handled by PB, handled in ScoreGoalDeserializer. */
    public static final class ScoreGoalCase {

        private StageChangeReason result;
        private double goal;
        private Relational<Number> goalRelational;

        public ScoreGoalCase(StageChangeReason result, double goal, Relational<Number> goalRelational) {
            this.result = result;
            this.goal = goal;
            this.goalRelational = goalRelational;
        }

        public StageChangeReason getResult() {
            return result;
        }

        public double getGoal() {
            return goal;
        }

        public Relational<Number> getGoalRelational() {
            return goalRelational;
        }

        @Override
        public String toString() {
            return "ScoreGoalCase{" +
                "result=" + result +
                ", goal=" + goal +
                ", goalRelational=" + goalRelational +
                '}';
        }
    }

    /**
     *
     * <b>Case #1</b>: The following serialized goal-case may be seen as a major convenience to many people since all it takes is a simple integer:
     * <pre>
     * {
     *   "score": 12 // This means the goal is 12 points
     * }
     * </pre>
     *
     * <b>Case #2</b>: The following map defines a goal-case in general, see Case #2 for what might be preceding:<br />
     *
     * <pre>
     * {
     *   "score": {
     *      initial-score: 1,
     *      cases: {
     *        // Although optional, either one must be defined.
     *        fail: <0, 
     *        succeed: ">2"
     *      }
     *   }
     * }</pre>
     *
     * <b>Case #3</b>: Similar to case #2, a goal may be defined but applied to a specific filter, used to identify entities in the session. <br />
     * P.S. The filter may be a string id or definition (which would be unidentifiable).
     * <pre>
     * {
     *   "score": {
     *     "my_filter": {
     *       initial-score: 1,
     *       ...
     *     }
     *   }
     * }</pre>
     *
     * <b>Case #4</b>: Similar to case #1 and #3, where the value may be an integer but may also be applied to a specific filter.
     * <pre>
     * {
     *   "score": {
     *      "my_filter": 1
     *   }
     * }</pre>
     */
    private static final class ScoreGoalDeserializer implements BlurSerializer<List<ScoreGoalData>> {

        @Override
        public List<ScoreGoalData> deserialize(Object serialized, Class wantedType, SerializerSet serializerSet)
            throws IllegalArgumentException {
            if (serialized == null) {
                return Collections.emptyList();
            }

            List<ScoreGoalData> result = new ArrayList<>();
            FilterSerializer filterSerializer = (FilterSerializer) serializerSet.getClassSerializer(Filter.class);

            if (serialized instanceof List) { // List of goals
                for (Object o : ((List<Object>) serialized)) {
                    if (!(o instanceof Map)) {
                        filterSerializer.getManager().getLogger().severe("One goal is not in the correct format: " + o);
                        continue;
                    }

                    // Each entry in the list of _score_ must be in the form of a map.
                    Map<String, Object> map = (Map<String, Object>) o;

                    // This is a single entry map which consists of a filter represented as a String in the Key.
                    // See Case #2 and Case #3
                    if (map.size() == 1) {
                        Entry<String, Object> entry = map.entrySet().iterator().next();
                        Filter filter = filterSerializer.deserializeStringToFilter(entry.getKey());
                        result.add(doGeneral(entry.getValue(), filter, filterSerializer));
                    } else {
                        result.add(doMap(map, null, filterSerializer));
                    }
                }
            } else if (serialized instanceof Map) { // Single goal
                result.add(doMap((Map) serialized, null, filterSerializer));
            } else {
                /*
                 * Case #1
                 */
                result.add(doGeneral(serialized, null, null));
            }
            return result;
        }

        @SuppressWarnings("Duplicates")
        private ScoreGoalData doGeneral(Object o, Filter filter, FilterSerializer filterSerializer) {
            if (o instanceof Map) {
                /*
                 * Case #2
                 */
                return doMap(((Map) o), filter, filterSerializer);
            } else if (o instanceof String || o instanceof Number) {
                // Value provided is just a string, assume that-that is the goal to trigger OBJECTIVE_SUCCESS.
                /*
                 * Case #1
                 */
                ScoreGoalData scoreGoalData = new ScoreGoalData();
                scoreGoalData.cases.add(successCase(Double.parseDouble(o.toString())));
                return scoreGoalData;
            }
            return null;
        }

        private ScoreGoalCase successCase(Number number) {
            return new ScoreGoalCase(StageChangeReason.OBJECTIVE_SUCCESS, number.doubleValue(), Relationals.number(number));
        }

        private ScoreGoalCase failureCase(Number number) {
            return new ScoreGoalCase(StageChangeReason.OBJECTIVE_FAILED, number.doubleValue(), Relationals.number(number));
        }

        /**
         * Handles Case #2
         */
        @SuppressWarnings("Duplicates")
        private ScoreGoalData doMap(Map map, Filter filter, FilterSerializer filterSerializer) {
            ScoreGoalData scoreGoalData = new ScoreGoalData();

            // Optional filter may be provided in the map goal case.
            if (filter == null && filterSerializer != null && map.containsKey("filter")) {
                // Check if filter contains before doing this code as a precaution in case someone intentionally is setting filter to null.
                // Alternatively, ensure it is treated as absent, so that in the case of ScoreGoalData having a default Filter it wouldn't be overridden.
                filter = Optional.ofNullable(map.get("filter")).map(s -> filterSerializer.deserialize(s, null)).orElse(null);
                scoreGoalData.filter = filter;
            } else if (filter != null) {
                scoreGoalData.filter = filter;
            }

            // initial-score
            scoreGoalData.initialScore = Match.of(map.get("initial-score"))
                .whenType(Number.class).then(number -> number)
                .whenType(String.class).then(Double::parseDouble)
                .otherwise(0.).get().doubleValue();

            // Cases must be defined as that rationalises the whole configuration section.
            Preconditions.checkArgument(map.containsKey("cases"), "Cases must be defined in GoalModule.");
            Object cases = map.get("cases");
            if (cases instanceof Map) {
                Map casesMap = ((Map) cases);

                // Handle fail case
                if (casesMap.containsKey("fail")) {
                    String fail = casesMap.get("fail").toString();
                    double goal = Double.parseDouble(RelationalUtils.fixString(fail, RelationalUtils.operator(fail)));
                    scoreGoalData.cases.add(failureCase(goal));
                }

                // Handle succeed case
                if (casesMap.containsKey("succeed")) {
                    String succeed = casesMap.get("succeed").toString();
                    double goal = Double.parseDouble(RelationalUtils.fixString(succeed, RelationalUtils.operator(succeed)));
                    Relational<Number> relational = RelationalUtils.deserializeNumber(succeed);
                    scoreGoalData.cases.add(new ScoreGoalCase(StageChangeReason.OBJECTIVE_SUCCESS, goal, relational));
                }

                Preconditions.checkArgument(!scoreGoalData.cases.isEmpty(), "at least one case must be defined in GoalModule.");
            }

            return scoreGoalData;
        }
    }
}
