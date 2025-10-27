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
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlTransient
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.KeybindComponent
import net.md_5.bungee.api.chat.ScoreComponent
import net.md_5.bungee.api.chat.SelectorComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import net.md_5.bungee.api.chat.hover.content.Text

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class A : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()  // TODO: For some reason this is STILL being picked up by JAXB?! WHY IS IT IGNORING MY TRANSIENT?!

    @XmlAttribute(required = true)
    private lateinit var href: String

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder().event(ClickEvent(ClickEvent.Action.OPEN_URL, href))
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class B : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder().bold(true)
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Click : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var action: String? = null

    @XmlAttribute(required = true)
    private var value: String? = null

    override fun createBuilder(): ComponentBuilder {
        require(action != null) { "action attribute must be specified in <click>." }
        val action = Enums.findFuzzyByValue(ClickEvent.Action::class.java, action)
                ?: throw IllegalArgumentException("Invalid click action $action.")
        require(value != null) { "value attribute must be specified in <value>." }
        return ComponentBuilder().event(ClickEvent(action, value!!))
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Color : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var color: String? = null

    override fun createBuilder(): ComponentBuilder {
        require(color != null) { "color attribute must be specified in <color>." }
        val textColor = ChatColor.of(color)
        require(textColor != null) { "Invalid color $color" }
        return ComponentBuilder().color(textColor)
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Hover : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var action: String? = null

    @XmlAttribute(required = true)
    private var value: String? = null

    override fun createBuilder(): ComponentBuilder {
        require(action != null) { "action attribute must be specified in <hover>." }
        val action = Enums.findFuzzyByValue(HoverEvent.Action::class.java, action)
                ?: throw IllegalArgumentException("Invalid hover action $action.")
        require(value != null) { "value attribute must be specified in <value>." }
        val value: BaseComponent = try {
            TextParsers.XML_PARSER.parse(this.value!!)
        } catch (e: Exception) { // In case the value is not valid XML, 
            TextComponent(this.value!!)
        }
        return ComponentBuilder().event(HoverEvent(action, Text(arrayOf(value))))
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class I : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder().italic(true)
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Keybind : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()  // Overwritten in createBuilder()

    @XmlAttribute(required = true)
    private var key: String? = null

    override fun createBuilder(): ComponentBuilder {
        require(key != null) { "key attribute must be specified in <keybind>." }
        rootComponent = KeybindComponent(key!!)
        return ComponentBuilder()
    }

}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Obfuscated : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder().obfuscated(true)
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Score : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var name: String? = null

    @XmlAttribute(required = true)
    private var objective: String? = null

    @XmlAttribute
    private var value: String? = null
    override fun createBuilder(): ComponentBuilder {
        require(name != null) { "name attribute must be specified in <score>." }
        require(objective != null) { "objective attribute must be specified in <score>." }
        rootComponent = ScoreComponent(name!!, objective!!, value)
        return ComponentBuilder()
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Selector : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var pattern: String? = null
    override fun createBuilder(): ComponentBuilder {
        require(pattern != null) { "pattern attribute must be specified in <selector>." }
        rootComponent = SelectorComponent(pattern!!)
        return ComponentBuilder()
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Span : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder()
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Strike : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {
        return ComponentBuilder().strikethrough(true)
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class Tl : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    @XmlAttribute(required = true)
    private var key: String? = null

    override fun createBuilder(): ComponentBuilder {
        require(key != null) { "key attribute must be specified in <tl>." }
        val args = attr?.filter { it.key.localPart.startsWith("with-") }
                ?.toSortedMap(compareBy { it.toString() })
                ?.map { TextParsers.XML_PARSER.parse(it.value) }
        rootComponent = TranslatableComponent(key!!, *args!!.toTypedArray())
        return ComponentBuilder()
    }

}

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)  // Necessary so that BaseComponent isn't mapped by JAXB, which causes conflict with Java's `Color` and `Color` in this file.
class U : Element(), ComponentCreator {
    override var rootComponent: BaseComponent = TextComponent()

    override fun createBuilder(): ComponentBuilder {

        return ComponentBuilder().underlined(true)
    }
}
