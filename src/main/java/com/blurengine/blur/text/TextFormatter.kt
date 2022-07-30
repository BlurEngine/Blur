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

import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Text

object TextFormatter {
    val PATTERN = """(?<!\\)\{(\d+)}""".toPattern()
    var step = 0

    fun format(component: BaseComponent, args: Array<out Any?>, overwriteOriginal: Boolean = false): BaseComponent {
        val componentCopy = if (!overwriteOriginal) {
             component.duplicate()
        } else {
            component
        }

        // Most of these operations are on template components so don't overwrite the given component
        require(args.isNotEmpty()) { "args cannot be empty." }
        val componentArgs: Map<Int, BaseComponent> = args
                .mapIndexed { idx, it -> idx to it }
                .filter { it.second is BaseComponent }
                .map { it.first to it.second as BaseComponent }.toMap()

        when (componentCopy) {
            is KeybindComponent -> componentCopy.apply {
                this.keybind = replaceParams(this.keybind, args)
            }
            is ScoreComponent -> componentCopy.apply {
                this.name = replaceParams(this.name, args)
                this.objective = replaceParams(this.objective, args)
                this.value = replaceParams(this.value, args)
            }
            is SelectorComponent -> componentCopy.apply {
                this.selector = replaceParams(this.selector, args)
            }
            is TextComponent -> {
                if (componentArgs.isEmpty()) {  // Don't do all the messy stuff for a simple text replacement
                    componentCopy.apply {
                        text = replaceParams(componentCopy.text, args)
                        clickEvent = if (clickEvent != null) ClickEvent(clickEvent.action, replaceParams(clickEvent.value, args)) else null
                        hoverEvent = if (hoverEvent != null) formatHoverEvent(hoverEvent, args) else null
                    }
                } else {
                    componentCopy.apply {
                        clickEvent = if (clickEvent != null) ClickEvent(clickEvent.action, replaceParams(clickEvent.value, args)) else null
                        hoverEvent = if (hoverEvent != null) formatHoverEvent(hoverEvent, args) else null
                    }
                    val matcher = PATTERN.matcher(componentCopy.text)  // Find replacement tags
                    val sb = StringBuffer()  // Create an empty string buffer
                    val thisBuilder = ComponentBuilder()
                    var matchedAnything = false  // Set to true if any matches are found
                    while (matcher.find()) {  // Move along matches to the pattern
                        matchedAnything = true
                        val idx = matcher.group(1).toInt()
                        if (idx in componentArgs) {  // If we are to replace with a component
                            matcher.appendReplacement(sb, "") // Remove the matched pattern from the sequence
                            if (sb.isNotEmpty()) {
                                // First put the string buffer in the builder and copy formatting to it
                                thisBuilder.append(sb.toString()).currentComponent.apply {
                                    copyFormatting(componentCopy)
                                }
                                sb.setLength(0)  // Then empty the string buffer
                            }
                            val formattedComponent = format(componentArgs[idx]!!, args, overwriteOriginal = true)  // Format the component
                            thisBuilder.append(formattedComponent)  // Add the component
                        } else {  // Replace with a regular string if we have any arguments left, otherwise null
                            val str = if (idx > args.lastIndex) "null" else args[idx].toString()
                            matcher.appendReplacement(sb, str)
                        }
                    }

                    matcher.appendTail(sb)  // Get the rest of the string

                    if (sb.isNotEmpty()) {
                        // Stick it on the string builder and copy formatting to it
                        thisBuilder.append(sb.toString()).currentComponent.apply {
                            copyFormatting(componentCopy)
                        }
                    }

                    if (matchedAnything) {
                        componentCopy.apply {
                            text = ""  // Remove the actual text
                            extra = thisBuilder.create().toMutableList()  // Replace it with just the extras required
                        }
                    }
                }
            }
            is TranslatableComponent -> componentCopy.apply {
                this.translate = replaceParams(componentCopy.translate, args)
                this.with = this.with.map { format(it, args, overwriteOriginal = true) }
            }
            else -> throw UnsupportedOperationException("Unknown type ${componentCopy.javaClass.canonicalName}")
        }

        componentCopy.extra?.map { format(it, args, overwriteOriginal = true) }  // Format all children
        ++step  // WHAT THE HECK IS THIS FOR?!
        return componentCopy
    }

    private fun formatHoverEvent(hoverEvent: HoverEvent, args: Array<out Any?>): HoverEvent {
        return HoverEvent(hoverEvent.action, hoverEvent.contents.map {
            if (it is Text && it.value is String) {
                Text(replaceParams(it.value as String, args))
            } else if (it is Text && it.value is Collection<*>) {
                // TODO: MAKE THIS LESS JANKY
                Text(arrayOf((it.value as Collection<BaseComponent>).map {
                    format(it, args)
                }) as Array<out BaseComponent>)
            } else {
                it
            }

        })
    }

    private fun replaceParams(string: CharSequence, args: Array<out Any?>): String {
        val matcher = PATTERN.matcher(string)
        val sb = StringBuffer()
        while (matcher.find()) {
            val idx = matcher.group(1).toInt()
            val str = if (idx > args.lastIndex) "null" else args[idx].toString()
            matcher.appendReplacement(sb, str)
        }
        matcher.appendTail(sb)
        return sb.toString()
    }
}
