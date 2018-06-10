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

package com.blurengine.blur.text.xml;

import com.blurengine.blur.text.TextParsers;

import net.kyori.text.Component;
import net.kyori.text.KeybindComponent;
import net.kyori.text.ScoreComponent;
import net.kyori.text.SelectorComponent;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;

import org.junit.Assert;
import org.junit.Test;

public class XmlParserTest {

    @Test
    public void testStringOnly() {
        Component component = TextParsers.XML_PARSER.parse("foo");
        TextComponent expected = TextComponent.of("foo");
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testSpan() {
        Component component = TextParsers.XML_PARSER.parse("<span>foo</span>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo"));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testAnchor() {
        Component component = TextParsers.XML_PARSER.parse("<a href=\"bar\">foo</a>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo")
            .clickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "bar")));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testBold() {
        Component component = TextParsers.XML_PARSER.parse("<b>foo</b>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").decoration(TextDecoration.BOLD, true));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testColor() {
        Component component = TextParsers.XML_PARSER.parse("<color color=\"gray\">foo</color>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").color(TextColor.GRAY));
        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidColor() throws Exception {
        TextParsers.XML_PARSER.parse("<color color=\"BestC0l0R\">foo</color>");
    }

    @Test
    public void testItalic() {
        Component component = TextParsers.XML_PARSER.parse("<i>foo</i>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").decoration(TextDecoration.ITALIC, true));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testObfuscated() {
        Component component = TextParsers.XML_PARSER.parse("<obfuscated>foo</obfuscated>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").decoration(TextDecoration.OBFUSCATED, true));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testStrike() {
        Component component = TextParsers.XML_PARSER.parse("<strike>foo</strike>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").decoration(TextDecoration.STRIKETHROUGH, true));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testUnderline() {
        Component component = TextParsers.XML_PARSER.parse("<u>foo</u>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo").decoration(TextDecoration.UNDERLINE, true));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testHover() {
        Component component = TextParsers.XML_PARSER.parse("<hover action=\"show_text\" value=\"bar\">foo</hover>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo")
            .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("bar"))));
        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidHoverAction() throws Exception {
        TextParsers.XML_PARSER.parse("<hover action=\"bar\" value=\"ay\">foo</hover>");
    }

    @Test
    public void testClick() {
        Component component = TextParsers.XML_PARSER.parse("<hover action=\"show_text\" value=\"bar\">foo</hover>");
        TextComponent expected = TextComponent.of("").append(TextComponent.of("foo")
            .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("bar"))));
        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidClickAction() throws Exception {
        TextParsers.XML_PARSER.parse("<click action=\"bar\" value=\"ay\">foo</click>");
    }

    @Test
    public void testTranslatable() {
        Component component = TextParsers.XML_PARSER.parse("<tl key=\"chat.type.text\" with-1=\"SupaHam\" with-2=\"I'm the best!\">foo</tl>");
        TextComponent expected = TextComponent.of("").append(TranslatableComponent.of(
            "chat.type.text",
            TextComponent.of("SupaHam"),
            TextComponent.of("I'm the best!")
            ).append(TextComponent.of("foo"))
        );
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testKeybind() {
        Component component = TextParsers.XML_PARSER.parse("<keybind key=\"key.forward\">foo</keybind>");
        TextComponent expected = TextComponent.of("").append(KeybindComponent.of("key.forward").append(TextComponent.of("foo")));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testScore() {
        Component component = TextParsers.XML_PARSER.parse("<score name=\"SupaHam\" objective=\"bar\" value=\"baz\">foo</score>");
        TextComponent expected = TextComponent.of("").append(ScoreComponent.of("SupaHam", "bar", "baz").append(TextComponent.of("foo")));
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testSelector() {
        Component component = TextParsers.XML_PARSER.parse("<selector pattern=\"SupaHam\" >foo</selector>");
        TextComponent expected = TextComponent.of("").append(SelectorComponent.of("SupaHam").append(TextComponent.of("foo")));
        Assert.assertEquals(expected, component);
    }
}
