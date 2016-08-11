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
import com.blurengine.blur.modules.InvulnerableModule.InvulnerableData;
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.TickerTask;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.time.Duration;

@ModuleInfo(name = "Invulnerable", dataClass = InvulnerableData.class)
public class InvulnerableModule extends Module implements Listener {

    private final TickerTask task;
    private boolean invulnerable = true;

    public InvulnerableModule(ModuleManager moduleManager, InvulnerableData data) {
        super(moduleManager);
        this.task = newTask(() -> setInvulnerable(false)).delay(data.duration).build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (this.invulnerable && event.getEntity() instanceof Player) {
            BlurPlayer bp = getPlayer((Player) event.getEntity());
            if (isSession(bp.getSession())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public static final class InvulnerableData implements ModuleData {

        private Duration duration = Duration.ofDays(30);

        @Override
        public Module parse(ModuleManager moduleManager, SerializedModule serialized) throws ModuleParseException {
            if (serialized.getAsObject() instanceof String) {
                serialized.loadToField(this, "duration"); // convert and set string data to _duration_ field
            } else {
                serialized.load(this);
            }
            return new InvulnerableModule(moduleManager, this);
        }
    }
}
