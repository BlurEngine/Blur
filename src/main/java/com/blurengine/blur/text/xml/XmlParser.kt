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

package com.blurengine.blur.text.xml

import com.blurengine.blur.text.TextParser
import java.io.StringReader
import java.util.ArrayList
import java.util.Collections
import java.util.regex.Pattern
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.annotation.XmlAnyAttribute
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElementRef
import jakarta.xml.bind.annotation.XmlMixed
import jakarta.xml.bind.annotation.XmlSeeAlso
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import javax.xml.namespace.QName

class XmlParser : TextParser {

    override fun parse(source: String): BaseComponent {
        val source = "<span>$source</span>"

        try {
            val context = JAXBContext.newInstance(Element::class.java)
            val unmarshaller = context.createUnmarshaller()
            val tag = unmarshaller.unmarshal(StringReader(source)) as Element
            val builder = ComponentBuilder()
            tag.apply(builder)
            tag.loop(builder)
            return (tag as ComponentCreator).rootComponent.apply { extra = builder.create().toMutableList() }  // Use TextComponent as a container
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse: $source", e)
        }
    }
}

@XmlSeeAlso(
        A::class,
        B::class,
        Click::class,
        Color::class,
        Hover::class,
        I::class,
        Keybind::class,
        Obfuscated::class,
        Strike::class,
        Score::class,
        Selector::class,
        Span::class,
        Tl::class,
        U::class
)
open class Element {

    @XmlAttribute
    private val onClick: String? = null

    @XmlAttribute
    private val onHover: String? = null

    @XmlAttribute
    private val style: String? = null

    @XmlAttribute
    private val insertion: String? = null

    @XmlElementRef(type = Element::class)
    @XmlMixed
    var mixedContent: List<Any> = ArrayList()

    @XmlAnyAttribute
    var attr: Map<QName, String>? = null
        private set
        get() = if (field != null) Collections.unmodifiableMap(field) else null

    /**
     * This regex pattern validates the syntax for onClick/onHover chat events.
     * Click [here](http://www.regexr.com/38pjh) for fun.
     */
    private val FUNCTION_PATTERN = Pattern.compile("^([^(]+)\\([\"'](.*)[\"']\\)$")

    companion object {
        fun parseAndApplyStyle(builder: ComponentBuilder, style: String) {
            val styles = style.split("\\s*,\\s*") // blue, underline , bold
            for (_style in styles) {
                handleStyle(builder, _style)
            }
        }

        private fun handleStyle(builder: ComponentBuilder, style: String) {
            val stateSplit = style.split("\\s*:\\s*")
            val styleName = stateSplit[0]
            val stateSpecified = stateSplit.size > 1
            val state = (if (stateSpecified) stateSplit[1].toBoolean() else true)  // TODO: This would break for not set
            var found = false
            if (styleName in "klmno") { // k = obfuscated, l = bold, m = strikethrough, n = underlined, o = italic
                when (styleName) {
                    "k" -> builder.obfuscated(true)
                    "l" -> builder.bold(true)
                    "m" -> builder.strikethrough(true)
                    "n" -> builder.underlined(true)
                    "o" -> builder.italic(true)
                }
                found = true
            }

            if (!found) {
                found = handleColor(builder, styleName, state, stateSpecified)
            }
            if (!found) {  // TODO: Tidy this up, right now it's not been fully re-thought, just modified.
                for (char in style) {
                    val chatColor = ChatColor.getByChar(char)
                    require(chatColor != null) { "Invalid style '${char.toUpperCase()}'" }
                    if (chatColor == ChatColor.MAGIC) {
                        builder.obfuscated(true)
                        found = true
                        continue
                    } else {
                        builder.color(chatColor)
                        found = true
                        continue
                    }
                }
            }
            require(found) { "Invalid style '$styleName'" }
        }

        private fun handleColor(builder: ComponentBuilder, value: String, state: Boolean, stateSpecified: Boolean): Boolean {
            if (value.equals("color", ignoreCase = true)) {
                val state = if (!stateSpecified && state) null else state
                require(state != true) { "color style only accepts FALSE OR NOT_SET" }
                builder.color(null) // NOT_SET or FALSE resets color.
                return true
            } else {
                val textColor = if (value.length == 1) {
                    ChatColor.getByChar(value[0])
                } else {
                    return false
                    // ChatColor.of(value)  // Don't allow long colour names, only colour codes.
                }
                if (textColor != null) {
                    builder.color(textColor)
                    return true
                }
            }
            return false
        }
    }

    open fun apply(builder: ComponentBuilder) {
        /**
        Apply Element formatting to the given builder.
         */

        if (style != null) {
            parseAndApplyStyle(builder, style)
        }
        if (insertion != null) {
            builder.insertion(insertion)
        }
        if (onClick != null) {
            val matcher = FUNCTION_PATTERN.matcher(onClick)
            if (!matcher.matches()) {
                throw IllegalArgumentException("onClick syntax is invalid ('$onClick')")
            }
            val action = ClickEvent.Action.values().firstOrNull { it.toString() == matcher.group(1).uppercase() }
                    ?: throw IllegalArgumentException("${matcher.group(1)} is not a valid ClickEvent.Action")
            val data = String.format(matcher.group(2))
            builder.event(ClickEvent(action, data))
        }

        if (onHover != null) {
            val matcher = FUNCTION_PATTERN.matcher(onHover)
            if (!matcher.matches()) {
                throw IllegalArgumentException("onHover syntax is invalid ('$onHover')")
            }
            val action = HoverEvent.Action.values().firstOrNull { it.toString() == matcher.group(1).uppercase() }
                    ?: throw IllegalArgumentException("${matcher.group(1)} is not a valid HoverEvent.Action")
            val data = String.format(matcher.group(2))
            builder.event(HoverEvent(action, Text(data)))
        }
    }

    fun loop(builder: ComponentBuilder) {
        for (o in mixedContent) {
            if (o is String) {
                builder.append(TextComponent(o.toString()))
            } else if (o is Element) {
                val elBuilder = (o as? ComponentCreator)?.createBuilder() ?: builder
                o.apply(elBuilder)
                o.loop(elBuilder)
                if (elBuilder.parts.isNotEmpty()) {
                    builder.append((o as ComponentCreator).rootComponent.apply { extra = elBuilder.create().toMutableList() })  // Use container for correct nesting
                }
            } else {
                throw IllegalStateException("Unknown mixed content of type ${o.javaClass.canonicalName}")
            }
        }
    }
}

interface ComponentCreator {
    var rootComponent: BaseComponent

    fun createBuilder(): ComponentBuilder
}
