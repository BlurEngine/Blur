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

package com.blurengine.blur.modules.stages;

/**
 * An enumeration of reasons as to why a {@link StageManager} would change its stage.
 */
public enum StageChangeReason {
    /**
     * {@link Stage} has changed due to the {@link StageManager} loading up.
     */
    STAGES_START,
    /**
     * {@link Stage} has changed due to the objective being accomplished by a player/team.
     */
    OBJECTIVE_SUCCESS,
    /**
     * {@link Stage} has changed due to the objective being failed.
     * <p />
     * <b>Not to be confused with {@link #TIME_LIMIT}, which states time running out.</b>
     *
     * @see #TIME_LIMIT
     */
    OBJECTIVE_FAILED,
    /**
     * {@link Stage} has changed due to a time limit running out.
     */
    TIME_LIMIT,
    /**
     * {@link Stage} has changed due to a module such as {@link NextStageModule} being loaded.
     */
    MODULE_TRIGGERED,
    /**
     * {@link Stage} has changed due to an unforeseen event.
     */
    UNKNOWN;
}
