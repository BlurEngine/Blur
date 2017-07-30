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
import org.junit.Assert
import org.junit.Test

class TextFormatterTest {
    @Test
    fun testBasicEmptyArgs() {
        val baseActual = TextComponent.of("foo")
        val actual: Component = TextFormatter.format(baseActual, arrayOf())
        val expected = TextComponent.of("foo")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testComplexEmptyArgs() {
        val baseActual = TextComponent.of("foo ").append(TextComponent.of("bar"))
        val actual: Component = TextFormatter.format(baseActual, arrayOf())
        val expected = TextComponent.of("foo ").append(TextComponent.of("bar"))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testStringArg() {
        val arg = "bar"
        val baseActual = TextComponent.of("foo {0}")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent.of("foo $arg")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testComponentArg() {
        val arg = TextComponent.of("bar")
        val baseActual = TextComponent.of("foo {0}")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent.of("foo ").append(arg)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testMoreComplexComponentArg() {
        val arg = TextComponent.of("bar")
        val baseActual = TextComponent.of("foo {0} baz")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent.of("foo ").append(arg).append(TextComponent.of(" baz"))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testKeybindStringArg() {
        val arg = "bar"
        val baseActual = KeybindComponent.of("foo {0}")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = KeybindComponent.of("foo bar")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testScoreStringArg() {
        val arg = "bar"
        val arg2 = "baz"
        val arg3 = "foo"
        val baseActual = ScoreComponent.of("foo {0}", "bar {1}", "{2} baz")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg, arg2, arg3))
        val expected = ScoreComponent.of("foo $arg", "bar $arg2", "$arg3 baz")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testSelectorStringArg() {
        val arg = "bar"
        val baseActual = SelectorComponent.of("foo {0}")
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = SelectorComponent.of("foo bar")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testTranslatableStringArg() {
        val arg = "bar"
        val arg2 = "baz"
        val baseActual = TranslatableComponent.of("foo {0}", listOf(TextComponent.of("foo {1}")))
        val actual: Component = TextFormatter.format(baseActual, arrayOf(arg, arg2))
        val expected = TranslatableComponent.of("foo $arg", listOf(TextComponent.of("foo $arg2")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testStyles() {
        val arg = "bar"
        val baseActual = TextComponent.of("foo {0}")
                .color(TextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/run {0}"))
                .hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("/run {0}")))
        val actual = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent.of("foo $arg")
                .color(TextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/run $arg"))
                .hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("/run $arg")))
        Assert.assertEquals(expected, actual)
    }
}
