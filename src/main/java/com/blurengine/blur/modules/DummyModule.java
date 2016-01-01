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

/**
 * Dummy module to test module functionality.
 */
@ModuleInfo(name = "BlurDummy")
public class DummyModule extends Module {

    public DummyModule(ModuleManager moduleManager) {
        super(moduleManager);
        System.out.println("Constructed");
    }

    @Override
    public void load() {
        super.load();
        System.out.println("Loaded");
    }

    @Override
    public void enable() {
        super.enable();
        System.out.println("Enabled!");
    }
}
