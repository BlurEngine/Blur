/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.modules.message

import com.blurengine.blur.session.BlurPlayer
import net.md_5.bungee.api.chat.BaseComponent

interface Message {
    val messageNode: String
    val component: BaseComponent

    fun send(blurPlayer: BlurPlayer, vararg args: Any?)
}

abstract class AbstractMessage(override val messageNode: String, override val component: BaseComponent) : Message
