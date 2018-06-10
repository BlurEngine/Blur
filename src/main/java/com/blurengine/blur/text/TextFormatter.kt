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

import net.kyori.text.BuildableComponent
import net.kyori.text.Component
import net.kyori.text.KeybindComponent
import net.kyori.text.ScoreComponent
import net.kyori.text.SelectorComponent
import net.kyori.text.TextComponent
import net.kyori.text.TranslatableComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent

object TextFormatter {
    val PATTERN = """(?<!\\)\{(\d+)}""".toPattern()
    var step = 0
    fun format(component: Component, args: Array<out Any?>): Component {
        require(args.isNotEmpty()) { "args cannot be empty." }
        return formatBuilder(component, args).build()
    }

    fun formatBuilder(component: Component, args: Array<out Any?>): BuildableComponent.Builder<*, *> {
        require(args.isNotEmpty()) { "args cannot be empty." }
        val componentArgs: Map<Int, Component> = args
                .mapIndexed { idx, it -> idx to it }
                .filter { it.second is Component }
                .map { it.first to it.second as Component }.toMap()

        // TODO update when extract-buildable is on master
        val builder: BuildableComponent.Builder<*, *> = when (component) {
            is KeybindComponent -> KeybindComponent.builder().also { _builder ->
                _builder.keybind(replaceParams(component.keybind(), args))
            }
            is ScoreComponent -> ScoreComponent.builder().also { _builder ->
                _builder.name(replaceParams(component.name(), args))
                _builder.objective(replaceParams(component.objective(), args))
                component.value()?.apply { _builder.value(replaceParams(this, args)) }
            }
            is SelectorComponent -> SelectorComponent.builder().also { _builder ->
                _builder.pattern(replaceParams(component.pattern(), args))
            }
            is TextComponent -> TextComponent.builder("").let { _builder ->
                var _builder = _builder
                val matcher = PATTERN.matcher(component.content())
                val sb = StringBuffer()
                var lastWasContent: Boolean? = null
                while(matcher.find()) {
                    val idx = matcher.group(1).toInt()
                    if (idx in componentArgs) {
                        matcher.appendReplacement(sb, "")
                        if (sb.isNotEmpty()) {
                            if (lastWasContent != false) {
                                lastWasContent = true
                                _builder.content(sb.toString())
                            } else {
                                _builder.append(TextComponent.of(sb.toString()))
                            }
                            sb.setLength(0)
                        }
                        val formattedComponentBuilder = formatBuilder(componentArgs[idx]!!, args)
                        if (lastWasContent == null && formattedComponentBuilder is TextComponent.Builder) {
                            _builder = formattedComponentBuilder
                        } else {
                            _builder.append(formattedComponentBuilder.build())
                        }
                        lastWasContent = false
                    } else {
                        val str = if (idx > args.lastIndex) "null" else args[idx].toString()
                        matcher.appendReplacement(sb, str)
                    }
                }

                matcher.appendTail(sb)

                if (sb.isNotEmpty()) {
                    if (lastWasContent != false) {
                        _builder.content(sb.toString())
                    } else {
                        _builder.append(TextComponent.of(sb.toString()))
                    }
                }
                _builder
            }
            is TranslatableComponent -> TranslatableComponent.builder().let { _builder ->
                _builder.key(replaceParams(component.key(), args))
                _builder.args(component.args().map { format(it, args) })
            }
            else -> throw UnsupportedOperationException("Unknown type ${component.javaClass.canonicalName}")
        }
        builder.mergeColor(component)
        builder.mergeDecorations(component)
        
        component.hoverEvent()?.apply {
            builder.hoverEvent(HoverEvent(action(), format(value(), args)))
        }
        
        component.clickEvent()?.apply {
            builder.clickEvent(ClickEvent(action(), replaceParams(value(), args)))
        }
        
        
        for (child in component.children()) {
            builder.append(format(child, args))
        }
        ++step
        return builder
    }

    private fun replaceParams(string: CharSequence, args: Array<out Any?>): String {
        val matcher = PATTERN.matcher(string)
        val sb = StringBuffer()
        while(matcher.find()) {
            val idx = matcher.group(1).toInt()
            val str = if (idx > args.lastIndex) "null" else args[idx].toString()
            matcher.appendReplacement(sb, str)
        }
        matcher.appendTail(sb)
        return sb.toString()
    }
}
