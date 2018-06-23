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
import com.supaham.commons.Enums
import net.kyori.text.BuildableComponent
import net.kyori.text.Component
import net.kyori.text.TextComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent
import net.kyori.text.format.TextColor
import net.kyori.text.format.TextDecoration
import org.bukkit.ChatColor
import java.io.StringReader
import java.util.ArrayList
import java.util.Collections
import java.util.regex.Pattern
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.XmlAnyAttribute
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElementRef
import javax.xml.bind.annotation.XmlMixed
import javax.xml.bind.annotation.XmlSeeAlso
import javax.xml.namespace.QName

class XmlParser : TextParser {

    override fun parse(source: String): Component {
        val source = "<span>$source</span>"

        try {
            val context = JAXBContext.newInstance(Element::class.java)
            val unmarshaller = context.createUnmarshaller()
            val tag = unmarshaller.unmarshal(StringReader(source)) as Element
            val builder = TextComponent.builder().content("")
            tag.apply(builder)
            tag.loop(builder)
            return builder.build()
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
        fun <C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> parseAndApplyStyle(builder: BuildableComponent.Builder<C, B>, style: String) {
            val styles = style.split("\\s*,\\s*") // blue, underline , bold
            for (_style in styles) {
                handleStyle(builder, _style)
            }
        }

        private fun <C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> handleStyle(builder: BuildableComponent.Builder<C, B>, style: String) {
            val stateSplit = style.split("\\s*:\\s*")
            val styleName = stateSplit[0]
            val stateSpecified = stateSplit.size > 1
            val state = (if (stateSpecified) Enums.findFuzzyByValue(TextDecoration.State::class.java, stateSplit[1]) else TextDecoration.State.TRUE)
                    ?: throw IllegalArgumentException("Invalid state: '${stateSplit[1]}'")
            var found = false
            for (decoration in TextDecoration.values()) {
                if (styleName == decoration.name) {
                    builder.decoration(decoration, state)
                    found = true
                    break
                }
            }

            if (!found) {
                found = handleColor(builder, styleName, state, stateSpecified)
            }
            if (!found) {
                for (char in style) {
                    val chatColor = ChatColor.getByChar(char)
                    require(chatColor != null) { "Invalid style '${char.toUpperCase()}'" }
                    val textDeco: TextDecoration?
                    if (chatColor == ChatColor.MAGIC) {
                        textDeco = TextDecoration.OBFUSCATED
                    } else {
                        textDeco = Enums.findFuzzyByValue(TextDecoration::class.java, chatColor.name)
                    }
                    if (textDeco != null) {
                        builder.decoration(textDeco, state)
                        found = true
                        continue
                    }
                    val textColor = Enums.findFuzzyByValue(TextColor::class.java, chatColor.name)
                    if (textColor != null) {
                        builder.color(textColor)
                        found = true
                        continue
                    }
                }
            }
            require(found) { "Invalid style '$styleName'" }
        }

        private fun <C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> handleColor(builder: BuildableComponent.Builder<C, B>, value: String,
                                                                             state: TextDecoration.State, stateSpecified: Boolean): Boolean {
            if (value.equals("color", ignoreCase = true)) {
                val state = if (!stateSpecified && state == TextDecoration.State.TRUE) TextDecoration.State.NOT_SET else state
                require(state != TextDecoration.State.TRUE) { "color style only accepts FALSE OR NOT_SET" }
                builder.color(null) // NOT_SET or FALSE resets color.
                return true
            } else {
                val textColor = Enums.findFuzzyByValue(TextColor::class.java, value)
                if (textColor != null) {
                    builder.color(textColor)
                    return true
                }
            }
            return false
        }
    }

    open fun <C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> apply(builder: BuildableComponent.Builder<C, B>) {
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
            val action = ClickEvent.Action.values().firstOrNull { it.toString() == matcher.group(1) }
                    ?: throw IllegalArgumentException("${matcher.group(1)} is not a valid ClickEvent.Action")
            val data = String.format(matcher.group(2))
            builder.clickEvent(ClickEvent(action, data))
        }

        if (onHover != null) {
            val matcher = FUNCTION_PATTERN.matcher(onHover)
            if (!matcher.matches()) {
                throw IllegalArgumentException("onHover syntax is invalid ('$onHover')")
            }
            val action = HoverEvent.Action.values().firstOrNull { it.toString() == matcher.group(1) }
                    ?: throw IllegalArgumentException("${matcher.group(1)} is not a valid HoverEvent.Action")
            val data = String.format(matcher.group(2))
            builder.hoverEvent(HoverEvent(action, TextComponent.of(data)))
        }
    }

    fun <C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> loop(builder: BuildableComponent.Builder<C, B>) {
        var elementAppended = false // Maintains order of string content when attempting to optimise.
        for (o in mixedContent) {
            if (o is String) {
                if (builder is TextComponent.Builder) {
                    val component = builder.build()
                    if (!elementAppended && component.content().isNullOrEmpty()) {
                        builder.content(o.toString())
                    } else {
                        builder.append(TextComponent.of(o.toString()))
                    }
                } else {
                    builder.append(TextComponent.of(o.toString()))
                }
            } else if (o is Element) {
                elementAppended = true
                val elBuilder = (o as? ComponentCreator<C, B>)?.createBuilder() ?: builder
                o.apply(elBuilder)
                o.loop(elBuilder)
                builder.append(elBuilder.build())
            } else {
                throw IllegalStateException("Unknown mixed content of type ${o.javaClass.canonicalName}")
            }
        }
    }
}

interface ComponentCreator<C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>> {
    fun createBuilder(): BuildableComponent.Builder<C, B>
}
