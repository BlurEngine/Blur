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
import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.SingleSound;

import org.bukkit.Sound;

/**
 * Represents a countdown that sets the player's exp levels as the remaining seconds. A {@link Sound#CLICK} sound is played if 10 or less seconds
 * remain.
 */
public class GlobalGameCountdown extends CountdownWithEXP {

    private static final SingleSound CLICK_SOUND = new SingleSound(Sound.CLICK);

    /**
     * Constructs a new global game countdown effect. This is equivalent to calling {@link #GlobalGameCountdown(Module, int, boolean)} with
     * the boolean as false.
     *
     * @param module module to own this component
     * @param ticks ticks to set
     */
    public GlobalGameCountdown(Module module, int ticks) {
        this(module, ticks, false);
    }

    /**
     * Constructs a new global game countdown effect.
     *
     * @param module module to own this component
     * @param ticks ticks to set
     * @param increment whether to increment the bar
     */
    public GlobalGameCountdown(Module module, int ticks, boolean increment) {
        super(module, ticks, increment);
    }

    @Override
    public void onTick(BlurPlayer player) {
        super.onTick(player);
        if (getTicks() % 20 == 0 && (getTicks() / 20) <= 10) {
            CLICK_SOUND.play(player.getPlayer());
        }
    }
}
