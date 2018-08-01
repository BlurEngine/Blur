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

import com.blurengine.blur.modules.extents.Extent;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.ModuleParseException;
import com.blurengine.blur.framework.SerializedModule;
import com.blurengine.blur.framework.WorldModule;
import com.blurengine.blur.modules.SetBlocksModule.SetBlocksData;
import com.blurengine.blur.framework.ModuleData;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "BSetBlocks", dataClass = SetBlocksData.class)
public class SetBlocksModule extends WorldModule {

    private final SetBlocksData data;

    public SetBlocksModule(ModuleManager moduleManager, SetBlocksData data) {
        super(moduleManager);
        this.data = data;
    }

    @Override
    public void enable() {
        Runnable runnable = () -> {
            for (Extent extent : data.extents) {
                for (BlockVector bv : extent) {
                    bv.toLocation(getSession().getWorld()).getBlock().setBlockData(data.blockData, data.physics);
                }
            }
        };
        // If a delay or interval has been set, create a future task
        if (data.delay != null || data.interval != null) {
            newTask().run(runnable).delay(data.delay).interval(data.interval).build();
        } else { // Otherwise, just run this module now
            runnable.run();
        }
    }

    public static final class SetBlocksData implements ModuleData {

        private List<Extent> extents = new ArrayList<>();
        private BlockData blockData = Material.STONE.createBlockData();
        private Duration delay;
        private Duration interval;
        private boolean physics = true;

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            serialized.load(this);
            check(!extents.isEmpty(), "No extents defined.");
            checkNotNull(blockData, "No extents defined.");
            return new SetBlocksModule(moduleManager, this);
        }
    }
}
