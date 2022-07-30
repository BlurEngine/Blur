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
import com.blurengine.blur.session.BlurSession;
import com.supaham.commons.bukkit.Colors;
import com.supaham.commons.bukkit.commands.flags.Flag;
import com.supaham.commons.bukkit.commands.flags.FlagParseResult;
import com.supaham.commons.bukkit.commands.flags.FlagParser;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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

    private static BaseComponent HEADER;
    private static BaseComponent FOOTER;
    private final BlurSession session;
    private final Blur blur;
    private FlagParser blurFlagParser;

    static {
        StringBuilder sb = new StringBuilder();
        IntConsumer consumer = i -> {
            ChatColor color = i % 2 == 0 ? ChatColor.BLUE : ChatColor.DARK_PURPLE;
            sb.append(color).append(ChatColor.STRIKETHROUGH).append('=');
        };
        IntStream.range(0, 15).forEach(consumer); // Add 15 padding
        HEADER = new TextComponent(new ComponentBuilder(sb.toString() + " ").append("Blur").color(net.md_5.bungee.api.ChatColor.YELLOW).bold(true)
                .append(" " + sb.toString(), ComponentBuilder.FormatRetention.NONE).create());

        IntStream.range(1, 21).forEach(consumer); // start from 1 to start with different colors, then add 20 =. 
        FOOTER = new TextComponent(sb.toString());
    }

    public BlurCommands(BlurSession session) {
        this.session = session;
        this.blur = session.getBlur();
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
            sender.spigot().sendMessage(HEADER);
        } else {
            sender.spigot().sendMessage(HEADER);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Blur version", this.blur.getVersion());
        map.put("Current sessions", this.blur.getSessionManager().getBlurSessions().size());
        map.forEach((k, v) -> sender.sendMessage(ChatColor.YELLOW + k + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + v));
        if (sender instanceof ConsoleCommandSender) {
            sender.spigot().sendMessage(FOOTER);
        } else {
            sender.spigot().sendMessage(FOOTER);
        }
    }
}
