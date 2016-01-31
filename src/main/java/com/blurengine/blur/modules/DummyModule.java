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

package com.blurengine.blur.modules;

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.modules.framework.ticking.BAutoInt;
import com.blurengine.blur.modules.framework.ticking.Tick;
import com.blurengine.blur.modules.framework.ticking.TickField;

/**
 * Dummy module to test module functionality.
 * <p>
 *     <b>This is just an example class and shouldn't actually be used in production.</b>
 * </p>
 */
@ModuleInfo(name = "BlurDummy")
public class DummyModule extends Module {

    // Increments every tick (by default, See value parameter).
    @TickField(increment = true)
    private BAutoInt ticks;
    @TickField(amount = -10) // increments 10 every tick because increment = false (subtract) and amount is a negative number.
    private BAutoInt ticks2;

    public DummyModule(ModuleManager moduleManager) {
        super(moduleManager);
        getLogger().info("Constructed");
    }

    @Override
    public void load() {
        super.load();
        getLogger().info("Loaded");
        displayTicks();
    }

    @Override
    public void enable() {
        super.enable();
        getLogger().info("Enabled!");
        displayTicks();
    }
    
    // This method is automatically called every second after the first second. See Tickable interface for more information.
    @Tick(interval = "1ms", delay = "1s")
    private void displayTicks() {
        getLogger().info("ticks: " + ticks.get());
        getLogger().info("ticks2: " + ticks2.get());
    }
}
