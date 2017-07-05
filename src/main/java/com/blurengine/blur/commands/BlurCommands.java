/*
 * Copyright 2016 Ali Moghnieh
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

package com.blurengine.blur.commands;

import com.blurengine.blur.Blur;
import com.supaham.commons.bukkit.Colors;
import com.supaham.commons.bukkit.commands.flags.Flag;
import com.supaham.commons.bukkit.commands.flags.FlagParseResult;
import com.supaham.commons.bukkit.commands.flags.FlagParser;
import com.supaham.commons.bukkit.utils.ChatUtils;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Optional;

public class BlurCommands extends BaseCommand {

    private static Component HEADER;
    private static Component FOOTER;
    private final Blur blur;
    private FlagParser blurFlagParser;

    static {
        StringBuilder sb = new StringBuilder();
        IntConsumer consumer = i -> {
            ChatColor color = i % 2 == 0 ? ChatColor.BLUE : ChatColor.DARK_PURPLE;
            sb.append(color).append(ChatColor.STRIKETHROUGH).append('=');
        };
        IntStream.range(0, 15).forEach(consumer); // Add 15 padding
        HEADER = TextComponent.of(sb.toString() + " ")
            .append(TextComponent.of("Blur").color(TextColor.YELLOW).decoration(TextDecoration.BOLD, true))
            .append(TextComponent.of(" " + sb.toString()).resetStyle());

        IntStream.range(1, 21).forEach(consumer); // start from 1 to start with different colors, then add 20 =. 
        FOOTER = TextComponent.of(sb.toString());
    }

    public BlurCommands(Blur blur) {
        this.blur = blur;
    }

    @CommandAlias("blur")
    public void blur(CommandSender sender, @Optional String[] args) {
        if (blurFlagParser == null) {
            blurFlagParser = new FlagParser();
            blurFlagParser.add(new Flag('v', "version", true, false));
        }
        FlagParseResult flags = blurFlagParser.parse(args);
        if (!Blur.isDev(sender)) {
            sender.sendMessage(Colors._darkGreen("This server is powered by ").yellow("Blur").darkGreen(".").toString());
            return;
        }
        if (flags.contains('v')) {
            sender.sendMessage("Blur version: " + this.blur.getVersion());
            return;
        }
        if (sender instanceof ConsoleCommandSender) {
            ChatUtils.sendStringComponent(sender, HEADER);
        } else {
            ChatUtils.sendComponent(sender, HEADER);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Blur version", this.blur.getVersion());
        map.put("Current sessions", this.blur.getSessionManager().getBlurSessions().size());
        map.forEach((k, v) -> sender.sendMessage(ChatColor.YELLOW + k + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + v));
        if (sender instanceof ConsoleCommandSender) {
            ChatUtils.sendStringComponent(sender, FOOTER);
        } else {
            ChatUtils.sendComponent(sender, FOOTER);
        }
    }
}
