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

import com.blurengine.blur.text.TextParsers
import com.supaham.commons.Enums
import net.kyori.text.Component
import net.kyori.text.KeybindComponent
import net.kyori.text.ScoreComponent
import net.kyori.text.SelectorComponent
import net.kyori.text.TextComponent
import net.kyori.text.TranslatableComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent
import net.kyori.text.format.TextColor
import net.kyori.text.format.TextDecoration
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
class A : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    @XmlAttribute(required = true)
    private lateinit var href: String

    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("").clickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, href))
    }
}

@XmlRootElement
class B : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("").decoration(TextDecoration.BOLD, true)
    }
}

@XmlRootElement
class Click : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    @XmlAttribute(required = true)
    private var action: String? = null
    @XmlAttribute(required = true)
    private var value: String? = null

    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        require(action != null) { "action attribute must be specified in <click>." }
        val action = Enums.findFuzzyByValue(ClickEvent.Action::class.java, action) ?: throw IllegalArgumentException("Invalid click action $action.")
        require(value != null) { "value attribute must be specified in <value>." }
        return TextComponent.builder().content("").clickEvent(ClickEvent(action, value!!))
    }
}

@XmlRootElement
class Color : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    @XmlAttribute(required = true)
    private var color: String? = null

    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        require(color != null) { "color attribute must be specified in <color>." }
        val textColor = Enums.findFuzzyByValue(TextColor::class.java, color)
                ?: TextColor.values().firstOrNull { it.toString().equals(color, ignoreCase = true) }
        require(textColor != null) { "Invalid color $color" }
        return TextComponent.builder().content("").color(textColor)
    }
}

@XmlRootElement
class Hover : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    @XmlAttribute(required = true)
    private var action: String? = null
    @XmlAttribute(required = true)
    private var value: String? = null

    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        require(action != null) { "action attribute must be specified in <hover>." }
        val action = Enums.findFuzzyByValue(HoverEvent.Action::class.java, action) ?: throw IllegalArgumentException("Invalid hover action $action.")
        require(value != null) { "value attribute must be specified in <value>." }
        var value: Component
        try {
            value = TextParsers.XML_PARSER.parse(this.value!!)
        } catch (e: Exception) { // In case the value is not valid XML, 
            value = TextComponent.of(this.value!!)
        }
        return TextComponent.builder().content("").hoverEvent(HoverEvent(action, value))
    }
}

@XmlRootElement
class I : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("").decoration(TextDecoration.ITALIC, true)
    }
}

@XmlRootElement
class Keybind : Element(), ComponentCreator<KeybindComponent.Builder, KeybindComponent> {
    @XmlAttribute(required = true)
    private var key: String? = null

    override fun createBuilder(): Component.Builder<KeybindComponent.Builder, KeybindComponent> {
        require(key != null) { "key attribute must be specified in <keybind>." }
        return KeybindComponent.builder().keybind(key!!)
    }

}

@XmlRootElement
class Obfuscated : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("").decoration(TextDecoration.OBFUSCATED, true)
    }
}

@XmlRootElement
class Score : Element(), ComponentCreator<ScoreComponent.Builder, ScoreComponent> {
    @XmlAttribute(required = true)
    private var name: String? = null
    @XmlAttribute(required = true)
    private var objective: String? = null
    @XmlAttribute
    private var value: String? = null
    override fun createBuilder(): Component.Builder<ScoreComponent.Builder, ScoreComponent> {
        require(name != null) { "name attribute must be specified in <score>." }
        require(objective != null) { "objective attribute must be specified in <score>." }
        return ScoreComponent.builder().name(name!!).objective(objective!!).value(value)
    }
}

@XmlRootElement
class Selector : Element(), ComponentCreator<SelectorComponent.Builder, SelectorComponent> {
    @XmlAttribute(required = true)
    private var pattern: String? = null
    override fun createBuilder(): Component.Builder<SelectorComponent.Builder, SelectorComponent> {
        require(pattern != null) { "pattern attribute must be specified in <selector>." }
        return SelectorComponent.builder().pattern(pattern!!)
    }
}

@XmlRootElement
class Span : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("")
    }
}

@XmlRootElement
class Strike : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        val component = TextComponent.builder().content("").decoration(TextDecoration.STRIKETHROUGH, true)
        return component
    }
}

@XmlRootElement
class Tl : Element(), ComponentCreator<TranslatableComponent.Builder, TranslatableComponent> {
    @XmlAttribute(required = true)
    private var key: String? = null

    override fun createBuilder(): Component.Builder<TranslatableComponent.Builder, TranslatableComponent> {
        require(key != null) { "key attribute must be specified in <tl>." }
        val component = TranslatableComponent.builder().key(key!!)
        val args = attr?.filter { it.key.localPart.startsWith("with-") }
                ?.toSortedMap(compareBy { it.toString() })
                ?.map { TextParsers.XML_PARSER.parse(it.value) }
        if (args != null) {
            component.args(args)
        }
        return component
    }

}

@XmlRootElement
class U : Element(), ComponentCreator<TextComponent.Builder, TextComponent> {
    override fun createBuilder(): Component.Builder<TextComponent.Builder, TextComponent> {
        return TextComponent.builder().content("").decoration(TextDecoration.UNDERLINE, true)
    }
}
