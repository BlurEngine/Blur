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

import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleManager;
import com.blurengine.blur.session.BlurPlayer;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Represents a countdown with a {@link BlurPlayer}'s experience bar. That is, {@link Player#getExp()}.
 */
public class CountdownWithEXP extends AbstractCountdown {

    private Supplier<Collection<BlurPlayer>> supplier;
    private boolean increment;
    private float incrementation;

    public CountdownWithEXP(Module module, int ticks, boolean increment) {
        this(module, ticks, module::getPlayers, increment);
    }

    public CountdownWithEXP(Module module, int ticks, @Nonnull Supplier<Collection<BlurPlayer>> supplier, boolean increment) {
        super(module, ticks);
        this.supplier = supplier;
        this.increment = increment;
        this.incrementation = 1.0f / ticks;
        supplier.get().forEach(p -> p.getPlayer().setExp(this.increment ? 0f : 1f));
    }

    @Override
    public void onCancel() {
        supplier.get().forEach(p -> p.getPlayer().setExp(this.increment ? 1f : 0f));
    }

    @Override
    public void onEnd() {
        supplier.get().forEach(p -> p.getPlayer().setExp(this.increment ? 1f : 0f));
    }

    @Override
    public void onTick() {
        supplier.get().stream().forEach(this::onTick);
    }

    @Override
    public void onTick(BlurPlayer player) {
        player.getPlayer().setExp(Math.min(getTicks() * incrementation, 1f));
    }
}
