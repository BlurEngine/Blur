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

import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleData;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.modules.FixedHungerModule.FixedHungerData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@ModuleInfo(name = "FixedHunger", dataClass = FixedHungerData.class)
public class FixedHungerModule extends Module {

    private final int hunger;

    // This constructor is used to read the module's data class, in this case FixedHungerData.
    // The constructor is called via reflection from ModuleManager#createModules(Collection).
    public FixedHungerModule(ModuleManager moduleManager, FixedHungerData data) {
        super(moduleManager);
        this.hunger = data.hunger;
    }

    @EventHandler public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(this.hunger);
    }

    public static final class FixedHungerData implements ModuleData {

        private int hunger = 20; // default value of 20.

        @Override public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            return new FixedHungerModule(moduleManager, this);
        }
    }

}
