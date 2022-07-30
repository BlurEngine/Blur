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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.junit.Assert;
import org.junit.Test;

public class XmlParserTest {

    @Test
    public void testStringOnly() {
        BaseComponent component = TextParsers.XML_PARSER.parse("foo");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        expected.addExtra(new TextComponent("foo"));  // Content
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testSpan() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<span>foo</span>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        container.addExtra(new TextComponent("foo"));  // Inner span component with text content
        expected.addExtra(container);
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testAnchor() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<a href=\"bar\">foo</a>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent a = new TextComponent("foo");  // a component with text content
        a.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "bar"));
        container.addExtra(a);
        expected.addExtra(container);
        Assert.assertEquals(expected, component);
    }

    @Test
    public void testBold() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<b>foo</b>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent b = new TextComponent("foo");  // b component with text content
        b.setBold(true);
        container.addExtra(b);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testColor() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<color color=\"gray\">foo</color>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent color = new TextComponent("foo");
        color.setColor(ChatColor.GRAY);
        container.addExtra(color);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidColor() throws Exception {
        TextParsers.XML_PARSER.parse("<color color=\"BestC0l0R\">foo</color>");
    }

    @Test
    public void testItalic() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<i>foo</i>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent i = new TextComponent("foo");  // i component with text content
        i.setItalic(true);
        container.addExtra(i);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testObfuscated() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<obfuscated>foo</obfuscated>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent obfuscated = new TextComponent("foo");  // obfuscated component with text content
        obfuscated.setObfuscated(true);
        container.addExtra(obfuscated);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testStrike() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<strike>foo</strike>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent strike = new TextComponent("foo");  // strike component with text content
        strike.setStrikethrough(true);
        container.addExtra(strike);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testUnderline() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<u>foo</u>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent u = new TextComponent("foo");  // u component with text content
        u.setUnderlined(true);
        container.addExtra(u);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testHover() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<hover action=\"show_text\" value=\"bar\">foo</hover>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent hover = new TextComponent("foo");  // hover component with hover event set

        // Extra layers required due to parsing of the value
        BaseComponent[] value = {new TextComponent()};
        value[0].addExtra(new TextComponent("bar"));
        hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(value)));

        container.addExtra(hover);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidHoverAction() throws Exception {
        TextParsers.XML_PARSER.parse("<hover action=\"bar\" value=\"ay\">foo</hover>");
    }

    @Test
    public void testClick() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<click action=\"run_command\" value=\"/bar\">foo</click>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent container = new TextComponent();  // Tag container
        BaseComponent click = new TextComponent("foo");  // click component with click event set
        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bar"));

        container.addExtra(click);
        expected.addExtra(container);

        Assert.assertEquals(expected, component);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidClickAction() throws Exception {
        TextParsers.XML_PARSER.parse("<click action=\"bar\" value=\"ay\">foo</click>");
    }

    @Test
    public void testTranslatable() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<tl key=\"chat.type.text\" with-1=\"SupaHam\" with-2=\"I'm the best!\">foo</tl>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)

        TextComponent with1 = new TextComponent();
        with1.addExtra(new TextComponent("SupaHam"));

        TextComponent with2 = new TextComponent();
        with2.addExtra(new TextComponent("I'm the best!"));

        BaseComponent tl = new TranslatableComponent("chat.type.text", with1, with2);  // tl component
        tl.addExtra(new TextComponent("foo"));  // Added to span because that's what it seems to be supposed to do?
        expected.addExtra(tl);

        // This is enormously stupid... however something about comparing the tl patterns screws up direct comparisons.
        // If it manages to succeed these assertions but still be wrong... well then it's earned it, and I give up.
        Assert.assertEquals(((TranslatableComponent) component.getExtra().get(0)).getWith(),
                ((TranslatableComponent) expected.getExtra().get(0)).getWith());
        Assert.assertEquals(component.getExtra().get(0).getExtra(), expected.getExtra().get(0).getExtra());
    }

    @Test
    public void testKeybind() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<keybind key=\"key.forward\">foo</keybind>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent keybind = new KeybindComponent("key.forward");
        keybind.addExtra(new TextComponent("foo"));
        expected.addExtra(keybind);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testScore() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<score name=\"SupaHam\" objective=\"bar\" value=\"baz\">foo</score>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent score = new ScoreComponent("SupaHam", "bar", "baz");
        score.addExtra(new TextComponent("foo"));
        expected.addExtra(score);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testSelector() {
        BaseComponent component = TextParsers.XML_PARSER.parse("<selector pattern=\"SupaHam\" >foo</selector>");
        BaseComponent expected = new TextComponent();  // Outer span component (automatically added)
        BaseComponent selector = new SelectorComponent("SupaHam");
        selector.addExtra(new TextComponent("foo"));
        expected.addExtra(selector);

        Assert.assertEquals(expected, component);
    }

    @Test
    public void testArbitraryParsing() {
        // Just don't error... please
        BaseComponent component = TextParsers.XML_PARSER.parse("<span style=\"e\" onHover=\"show_text(&quot;Next Page&quot;)\">[&gt;] </span>");
        BaseComponent component1 = TextParsers.XML_PARSER.parse("<span style=\"cl\">[WARNING] </span><span style=\"7\">You are playing on a pro server! Your experience may be affected negatively until you get familiar with the game.</span>\\\n" +
                "  <span style=\"e\"> Type <u onClick=\"run_command(''/server beginner'')\">/server beginner</u> to try playing beginner level first.\\\n" +
                "  <span style=\"8\" onClick=\"run_command(''/iacknowledgemyskill'')\"> [HIDE]</span></span>");
    }
}
