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

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleLoader;
import com.blurengine.blur.modules.framework.ModuleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "BlurStageManager")
public class StageManager extends Module {

    public static final String DEFAULT_STAGE_NAME = "Default";

    private final List<Stage> stages = new ArrayList<>();

    private Stage currentStage;
    private int stageIndex = -1;

    static {
        ModuleLoader.register(StagesModule.class);
    }

    public StageManager(ModuleManager moduleManager) {
        super(moduleManager);
    }

    @Override
    public void load() {
        super.load();
        nextStage(StageChangeReason.STAGES_START);
    }

    private void reset() {
        reset(StageChangeReason.UNKNOWN);
    }

    private void reset(StageChangeReason reason) {
        getLogger().finer("Resetting StageManager with %s reason.", reason);
        this.stageIndex = -1;
        setCurrentStage(null, reason);
    }

    public boolean nextStage(StageChangeReason changeReason) {
        Preconditions.checkState(!this.stages.isEmpty(), "No stages to go through.");
        Stage lastStage = this.currentStage;
        getLogger().finer("nextStage called with %s reason.", changeReason);

        // That was the last Stage.
        if (this.stageIndex >= this.stages.size() - 1) {
            getLogger().finer("Last stage, calling StageCompleteEvent");
            // This is the end, call the StagesCompleteEvent. - Adele
            getSession().callEvent(new StagesCompleteEvent(changeReason, lastStage));
            reset();
            return true;
        } else {
            Stage nextStage = this.stages.get(++this.stageIndex);// get next stage and update stageIndex.
            // Call StageEnterEvent if we actually have a new currentStage
            if (setCurrentStage(nextStage, changeReason)) {
                getSession().callEvent(new StageChangedEvent(changeReason, this.currentStage, lastStage));
            } else { // Otherwise the stage change was cancelled, terminate!
                return false;
            }
        }
        return true;
    }

    public boolean setCurrentStage(Stage newStage, StageChangeReason reason) {
        Stage oldStage = this.currentStage;
        // newStage is equivalent to oldStage, this includes null pointers.
        if (Objects.equals(newStage, oldStage)) {
            return false;
        }

        // If PreStageChangeEvent is cancelled after we call it, terminate the method, returning false.
        if (!getSession().callEvent(new PreStageChangeEvent(this, reason, newStage)).isCancelled()) {
            // Disable previous stage
            if (oldStage != null) {
                oldStage.getModules().forEach(this::removeSubmodule);
            }

            // Update currentStage field and load up its modules.
            this.currentStage = newStage;
            if (this.currentStage != null) {
                this.currentStage.getModules().forEach(this::addSubmodule);
            }
            return true;
        }
        return false;
    }

    public boolean addStage(Stage stage) {
        Preconditions.checkNotNull(stage, "stage cannot be null.");
        Preconditions.checkState(!this.stages.contains(stage), "Stage %s already exists.", stage.getName());
        return this.stages.add(stage);
    }

    public void addDefaultStage() {
        Preconditions.checkState(this.stages.isEmpty(), "Cannot add default stage when stages have been defined.");
        getLogger().fine("Adding default stage.");
        this.stages.add(new Stage(this, DEFAULT_STAGE_NAME, Collections.emptyList()));
    }

    /* ================================
     * >> GETTERS / SETTERS
     * ================================ */

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public List<Stage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public int getStageIndex() {
        return stageIndex;
    }
}
