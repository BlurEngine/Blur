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

import net.kyori.text.KeybindComponent
import net.kyori.text.ScoreComponent
import net.kyori.text.SelectorComponent
import net.kyori.text.TextComponent
import net.kyori.text.TranslatableComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent
import net.kyori.text.format.TextColor
import net.kyori.text.format.TextDecoration
import org.junit.Assert
import org.junit.Test

class ComponentBuildersTest {
    @Test
    fun testTextEmpty() {
        val actual = TextComponentBuilder().build()
        val expected = TextComponent.of("")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testText() {
        val actual = TextComponentBuilder {
            content("foo")
            color(TextColor.RED)
        }.build()
        val expected = TextComponent.of("foo").color(TextColor.RED)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestingText() {
        val actual = TextComponentBuilder("foo") {
            text("bar") {
                text("baz") {
                    text("foo")
                }
            }
        }.build()

        val expected = TextComponent.of("foo")
                .append(TextComponent.of("bar")
                        .append(TextComponent.of("baz")
                                .append(TextComponent.of("foo"))
                        )
                )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testDecorations() {
        val actual = TextComponentBuilder {
            content("foo")
            bold()
            text("bar") {
                strikethrough()
                text("baz") {
                    underline()
                    text("foo") {
                        obfuscated()
                        text("bar") {
                            italic()
                        }
                    }
                }
            }
        }.build()

        val expected = TextComponent.of("foo").decoration(TextDecoration.BOLD, true)
                .append(TextComponent.of("bar").decoration(TextDecoration.STRIKETHROUGH, true)
                        .append(TextComponent.of("baz").decoration(TextDecoration.UNDERLINE, true)
                                .append(TextComponent.of("foo").decoration(TextDecoration.OBFUSCATED, true)
                                        .append(TextComponent.of("bar").decoration(TextDecoration.ITALIC, true))
                                )
                        )
                )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestedFunctions() {
        val command = "/foo"
        val tooltip = "text"
        val insertion = "insertion"

        val actual = TextComponentBuilder {
            content("foo")
            color(TextColor.RED)
            text("bar") {
                tooltip(tooltip)
                text("baz") {
                    runCommand(command)
                    text("foo") {
                        insertion(insertion)
                    }
                }
            }
        }.build()

        val expected = TextComponent.of("foo").color(TextColor.RED)
                .append(TextComponent.of("bar").hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of(tooltip)))
                        .append(TextComponent.of("baz").clickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                                .append(TextComponent.of("foo").insertion(insertion))
                        )
                )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testKeybind() {
        val key = "key"
        val actual = KeybindComponentBuilder(key).build()
        val expected = KeybindComponent.of(key)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestedKeybind() {
        val key = "key"
        val actual = KeybindComponentBuilder(key) {
            keybind(key)
        }.build()
        val expected = KeybindComponent.of(key).append(KeybindComponent.of(key))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testScore() {
        val name = "foo"
        val objective = "bar"
        val value = "baz"
        val actual = ScoreComponentBuilder(name, objective, value).build()
        val expected = ScoreComponent.of(name, objective, value)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestedScore() {
        val name = "foo"
        val objective = "bar"
        val value = "baz"
        val actual = ScoreComponentBuilder(name, objective, value) {
            score(name, objective, value)
        }.build()
        val expected = ScoreComponent.of(name, objective, value).append(ScoreComponent.of(name, objective, value))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testSelector() {
        val pattern = "pattern"
        val actual = SelectorComponentBuilder(pattern).build()
        val expected = SelectorComponent.of(pattern)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestedSelector() {
        val pattern = "pattern"
        val actual = SelectorComponentBuilder(pattern) {
            selector(pattern)
        }.build()
        val expected = SelectorComponent.of(pattern).append(SelectorComponent.of(pattern))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testTranslatable() {
        val key = "foo"
        val args = arrayOf("bar")
        val actual = TranslatableComponentBuilder(key) {
            argsString(args.asIterable())
        }.build()
        val expected = TranslatableComponent.of(key, args.map(TextComponent::of))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNestedTranslatable() {
        val key = "foo"
        val args = arrayOf("bar")
        val actual = TranslatableComponentBuilder(key) {
            argsString(args.asIterable())
            translatable(key) { argsString(args.asIterable()) }
        }.build()
        val expected = TranslatableComponent.of(key, args.map(TextComponent::of)).append(TranslatableComponent.of(key, args.map(TextComponent::of)))
        Assert.assertEquals(expected, actual)
    }
}
