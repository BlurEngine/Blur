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

package com.blurengine.blur.text.dsl

import com.supaham.commons.bukkit.utils.ChatUtils
import net.kyori.text.BuildableComponent
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
import java.util.UUID

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
open class ComponentBuilder<C : BuildableComponent<*, *>, B : BuildableComponent.Builder<C, B>>(val builder: B) {

    fun build(): C = builder.build()

    fun append(builder: ComponentBuilder<*, *>, newline: Boolean = false) = apply {
        append(builder.build(), newline)
    }

    fun append(builder: BuildableComponent.Builder<*, *>, newline: Boolean = false) = apply {
        append(builder.build(), newline)
    }

    fun append(component: Component, newline: Boolean = false) = apply {
        this@ComponentBuilder.builder.append(component)
        if (newline) {
            this@ComponentBuilder.builder.append(ChatUtils.NEW_LINE)
        }
    }

    fun color(color: TextColor) = apply { builder.color(color) }

    fun obfuscated(state: TextDecoration.State = TextDecoration.State.TRUE) = decoration(TextDecoration.OBFUSCATED, state)

    fun bold(state: TextDecoration.State = TextDecoration.State.TRUE) = decoration(TextDecoration.BOLD, state)

    fun strikethrough(state: TextDecoration.State = TextDecoration.State.TRUE) = decoration(TextDecoration.STRIKETHROUGH, state)

    fun underline(state: TextDecoration.State = TextDecoration.State.TRUE) = decoration(TextDecoration.UNDERLINE, state)

    fun italic(state: TextDecoration.State = TextDecoration.State.TRUE) = decoration(TextDecoration.ITALIC, state)

    fun decoration(decoration: TextDecoration, state: TextDecoration.State = TextDecoration.State.TRUE) = apply {
        builder.decoration(decoration, state)
    }

    fun click(event: ClickEvent) = apply { builder.clickEvent(event) }

    fun click(action: ClickEvent.Action, value: String) = apply { builder.clickEvent(ClickEvent(action, value)) }

    fun hover(event: HoverEvent) = apply { builder.hoverEvent(event) }

    fun hover(action: HoverEvent.Action, value: Component) = apply { builder.hoverEvent(HoverEvent(action, value)) }

    fun hover(action: HoverEvent.Action, value: BuildableComponent.Builder<*, *>) = hover(action, value.build())

    fun hover(action: HoverEvent.Action, value: ComponentBuilder<*, *>) = hover(action, value.builder)

    fun insertion(insertion: String) = apply { builder.insertion(insertion) }

    fun tooltip(text: String) = apply { hover(HoverEvent.Action.SHOW_TEXT, TextComponent.of(text)) }

    fun tooltip(iterable: Iterable<String>) = apply {
        val it = iterable.iterator()
        val builder = TextComponent.builder().content("")
        if (it.hasNext()) {
            builder.content(it.next())
            while (it.hasNext()) {
                builder.append(TextComponent.of("\n${it.next()}"))
            }
        }
        hover(HoverEvent.Action.SHOW_TEXT, builder.build())
    }

    fun runCommand(command: String) = apply { click(ClickEvent.Action.RUN_COMMAND, command) }

    fun suggestCommand(command: String) = apply { click(ClickEvent.Action.SUGGEST_COMMAND, command) }

    fun changePage(page: Int) = apply { click(ClickEvent.Action.CHANGE_PAGE, page.toString()) }

    fun openUrl(url: String) = apply { click(ClickEvent.Action.OPEN_URL, url) }

    fun showEntity(name: String, uuid: UUID, type: String?) = apply {
        val json = StringBuilder("""{name:"$name",id:$uuid""")
        if (type != null) json.append(""",type:$type""")
        json.append("}")
        hover(HoverEvent.Action.SHOW_ENTITY, TextComponent.of(json.toString()))
    }

    fun text(builder: TextComponentBuilder.() -> Unit = {}) = apply { TextComponentBuilder().apply(builder) }

    fun text(content: String, builder: TextComponentBuilder.() -> Unit = {}) = apply {
        append(TextComponentBuilder().content(content).apply(builder))
    }

    fun keybind(builder: KeybindComponentBuilder.() -> Unit = {}) = apply { KeybindComponentBuilder().apply(builder) }

    fun keybind(key: String, builder: KeybindComponentBuilder.() -> Unit = {}) = apply {
        append(KeybindComponentBuilder().key(key).apply(builder))
    }

    fun score(builder: ScoreComponentBuilder.() -> Unit = {}) = apply { ScoreComponentBuilder().apply(builder) }

    fun score(name: String, objective: String, value: String? = null, builder: ScoreComponentBuilder.() -> Unit = {}) = apply {
        append(ScoreComponentBuilder().name(name).objective(objective).value(value).apply(builder))
    }

    fun selector(builder: SelectorComponentBuilder.() -> Unit = {}) = apply { SelectorComponentBuilder().apply(builder) }

    fun selector(pattern: String, builder: SelectorComponentBuilder.() -> Unit = {}) = apply {
        append(SelectorComponentBuilder().pattern(pattern).apply(builder))
    }

    fun translatable(builder: TranslatableComponentBuilder.() -> Unit = {}) = apply { TranslatableComponentBuilder().apply(builder) }

    fun translatable(key: String, args: Iterable<Component>? = null, builder: TranslatableComponentBuilder.() -> Unit = {}) = apply {
        val _builder = TranslatableComponentBuilder().key(key)
        if (args != null) _builder.args(args) // TODO update when text makes args nullable
        _builder.apply(builder)
        append(_builder)
    }
}

class TextComponentBuilder(content: String? = null, builder: TextComponentBuilder.() -> Unit = {})
    : ComponentBuilder<TextComponent, TextComponent.Builder>(TextComponent.builder().content("")) {
    init {
        if (content != null) content(content)
        builder()
    }

    fun content(content: String) = apply { builder.content(content) }
}

class KeybindComponentBuilder(keybind: String? = null, builder: KeybindComponentBuilder.() -> Unit = {})
    : ComponentBuilder<KeybindComponent, KeybindComponent.Builder>(KeybindComponent.builder()) {
    init {
        if (keybind != null) key(keybind)
        builder()
    }

    fun key(keybind: String) = apply { builder.keybind(keybind) }
}

class ScoreComponentBuilder(name: String? = null, objective: String? = null, value: String? = null, builder: ScoreComponentBuilder.() -> Unit = {})
    : ComponentBuilder<ScoreComponent, ScoreComponent.Builder>(ScoreComponent.builder()) {
    init {
        if (name != null) name(name)
        if (objective != null) objective(objective)
        if (value != null) value(value)
        builder()
    }

    fun name(name: String) = apply { builder.name(name) }
    fun objective(objective: String) = apply { builder.objective(objective) }
    fun value(value: String?) = apply { builder.value(value) }
}

class SelectorComponentBuilder(pattern: String? = null, builder: SelectorComponentBuilder.() -> Unit = {})
    : ComponentBuilder<SelectorComponent, SelectorComponent.Builder>(SelectorComponent.builder()) {
    init {
        if (pattern != null) pattern(pattern)
        builder()
    }

    fun pattern(pattern: String) = apply { builder.pattern(pattern) }
}

class TranslatableComponentBuilder(key: String? = null, args: Iterable<Component>? = null, builder: TranslatableComponentBuilder.() -> Unit = {})
    : ComponentBuilder<TranslatableComponent, TranslatableComponent.Builder>(TranslatableComponent.builder()) {
    init {
        if (key != null) key(key)
        if (args != null) args(args)
        builder()
    }

    fun key(key: String) = apply { builder.key(key) }

    fun args(args: Iterable<Component>) = apply {
        val list = args as? List ?: args.toList()
        builder.args(list)
    }

    fun argsString(args: Iterable<String>) = apply {
        builder.args(args.map(TextComponent::of).toList())
    }
}
