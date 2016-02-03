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

package com.blurengine.blur.countdown;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.AbstractComponent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ticking.Tick;

/**
 * Represents a countdown object.
 */
public abstract class AbstractCountdown extends AbstractComponent implements Countdown {

    private final int initialTicks;
    private final Module module;
    private int ticks;

    public AbstractCountdown(Module module, int ticks) {
        super(module.getModuleManager());
        this.module = module;
        Preconditions.checkArgument(ticks > 0, "ticks cannot be less than 1.");
        this.initialTicks = ticks;
        this.ticks = this.initialTicks;
    }
    
    public void start() {
        module.addSubcomponent(this);
    }

    public void stop() {
        module.removeSubcomponent(this);
    }

    @Tick
    public void tick() {
        onTick();
        if (--ticks <= 0) {
            onEnd();
            removeTickable(this);
        }
    }

    public int getTicks() {
        return ticks;
    }
}
