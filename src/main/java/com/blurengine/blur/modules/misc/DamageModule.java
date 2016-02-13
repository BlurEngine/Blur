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

package com.blurengine.blur.modules.misc;

import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.framework.WorldModule;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

@ModuleInfo(name = "Damage")
public class DamageModule extends WorldModule {

    public DamageModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void debugDamageAndHealth(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            System.out.println(event.getDamage() + " " + ((LivingEntity) event.getEntity()).getHealth());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void a(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            System.out.println(event.getDamage(DamageModifier.MAGIC));
        }
    }
}
