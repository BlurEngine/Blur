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

package com.blurengine.blur.modules.stages

interface StageChangeReason {
    val name: String
}
/**
 * An enumeration of reasons as to why a [StageManager] would change its stage.
 */
enum class StageChangeReasons : StageChangeReason {
    /**
     * [Stage] has changed due to the [StageManager] loading up.
     */
    STAGES_START,
    /**
     * [Stage] has changed due to the objective being accomplished by a player/team.
     */
    OBJECTIVE_SUCCESS,
    /**
     * [Stage] has changed due to the objective being failed.
     *
     *
     * **Not to be confused with [TIME_LIMIT], which states time running out.**
     *
     * @see TIME_LIMIT
     */
    OBJECTIVE_FAILED,
    /**
     * [Stage] has changed due to a time limit running out.
     */
    TIME_LIMIT,
    /**
     * [Stage] has changed due to a module such as [NextStageModule] being loaded.
     */
    MODULE_TRIGGERED,
    /**
     * Stage changing because of shutdown.
     */
    SHUTDOWN,
    /**
     * [Stage] has changed due to an unforeseen event.
     */
    UNKNOWN
}
