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

import com.blurengine.blur.utils.format
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Text
import org.junit.Assert
import org.junit.Test

class TextFormatterTest {
    @Test(expected = IllegalArgumentException::class)
    fun testBasicEmptyArgs() {
        val baseActual = TextComponent("foo")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf())
        val expected = TextComponent("foo")
        Assert.assertEquals(expected, actual)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testComplexEmptyArgs() {
        val baseActual = TextComponent("foo ").apply { extra = listOf(TextComponent("bar")) }
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf())
        val expected = TextComponent("foo ").apply { extra = listOf(TextComponent("bar")) }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testStringArg() {
        val arg = "bar"
        val baseActual = TextComponent("foo {0}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent("foo $arg")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testComponentArg() {
        val arg = TextComponent("bar")
        val baseActual = TextComponent("foo {0}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent().apply { extra = listOf(TextComponent("foo "), arg) }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testMoreComplexComponentArg() {
        val arg = TextComponent("bar")
        val baseActual = TextComponent("foo {0} baz")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent().apply {
            extra = listOf(TextComponent("foo "), arg, TextComponent(" baz"))
        }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testKeybindStringArg() {
        val arg = "bar"
        val baseActual = KeybindComponent("foo {0}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = KeybindComponent("foo bar")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testScoreStringArg() {
        val arg = "bar"
        val arg2 = "baz"
        val arg3 = "foo"
        val baseActual = ScoreComponent("foo {0}", "bar {1}", "{2} baz")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg, arg2, arg3))
        val expected = ScoreComponent("foo $arg", "bar $arg2", "$arg3 baz")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testSelectorStringArg() {
        val arg = "bar"
        val baseActual = SelectorComponent("foo {0}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = SelectorComponent("foo bar")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testTranslatableStringArg() {
        val arg = "bar"
        val arg2 = "baz"
        val baseActual = TranslatableComponent("foo {0}", listOf(TextComponent("foo {1}")))
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg, arg2))
        val expected = TranslatableComponent("foo $arg", listOf(TextComponent("foo $arg2")))
        // Just test the bits that we actually touch. Otherwise general equality fails due to an inscrutable difference
        // between expected.pattern and actual.pattern.
        Assert.assertEquals(expected.translate, (actual as TranslatableComponent).translate)
        Assert.assertEquals(expected.with, actual.with)
    }

    @Test
    fun testStyles() {
        val arg = "bar"
        val baseActual = TextComponent("foo {0}").apply {
            color = ChatColor.RED
            isBold = true
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/run {0}")
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/run {0}"))
        }
        val actual = TextFormatter.format(baseActual, arrayOf(arg))
        val expected = TextComponent("foo $arg").apply {
            color = ChatColor.RED
            isBold = true
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/run $arg")
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/run $arg"))
        }
        Assert.assertEquals(expected.hashCode(), actual.hashCode())  // Need to use hashCode due to list comparisons failing
    }

    @Test
    fun testComponentArgSameNode() {
        val arg1 = TextComponent("foo")
        val arg2 = TextComponent("baz")
        val baseActual = TextComponent("{0} bar {1}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg1, arg2))
        val expected = TextComponent().apply {
            extra = listOf(arg1, TextComponent(" bar "), arg2)
        }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testComponentArgSameNode2() {
        val arg1 = TextComponent("foo")
        val arg2 = "baz"
        val baseActual = TextComponent("{0} bar {1}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg1, arg2))
        val expected = TextComponent().apply {
            extra = listOf(arg1, TextComponent(" bar $arg2"))
        }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testFormatterExtension() {
        val arg = "bar"
        val baseActual = TextComponent("foo {0}")
        val actual: BaseComponent = baseActual.format(arg)
        val expected = TextComponent("foo $arg")
        Assert.assertEquals(expected, actual)
        Assert.assertEquals(baseActual, TextComponent("foo {0}"))  // Make sure the template isn't modified
    }

    @Test
    fun testDifferentTypeArgs() {
        val arg1 = "bar"
        val arg2 = 2
        val baseActual = TextComponent("foo {0} {1}")
        val actual: BaseComponent = TextFormatter.format(baseActual, arrayOf(arg1, arg2))
        val expected = TextComponent("foo $arg1 $arg2")
        Assert.assertEquals(expected, actual)
    }
}
