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

package com.blurengine.blur.text

import com.blurengine.blur.text.xml.XmlParser
import net.md_5.bungee.api.chat.BaseComponent

interface TextParser {

    /**
     * Parses a [String] using this Parser.
     * @param source source to parse
     * @return an instance of [BaseComponent] with the parsed `source`
     */
    fun parse(source: String): BaseComponent
}

object TextParsers {
    @JvmField val XML_PARSER = XmlParser()
}
