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

import com.blurengine.blur.framework.Component;
import com.blurengine.blur.session.BlurPlayer;

/**
 * Represents a countdown object that extends a Tickable object. Since this extends Tickable object, the implementor needs to take into account
 * that implementing this class may break any implementations that extend tickable.
 */
public interface Countdown extends Component {

    /**
     * Called when the countdown has expired.
     */
    void onEnd();

    /**
     * Called when this countdown ticks. This method is until the ticks are zero; when this countdown has finished.
     */
    void onTick();

    void onTick(BlurPlayer player);

    /**
     * Called when the countdown has been cancelled.
     */
    void onCancel();
}
