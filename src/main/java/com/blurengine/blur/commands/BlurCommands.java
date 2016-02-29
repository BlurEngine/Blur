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
import com.sk89q.intake.Command;
import com.sk89q.intake.parametric.annotation.Switch;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.supaham.commons.bukkit.Colors;
import com.supaham.commons.bukkit.text.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class BlurCommands {

    private static FancyMessage HEADER;
    private static FancyMessage FOOTER;
    private final Blur blur;

    static {
        StringBuilder sb = new StringBuilder();
        IntConsumer consumer = i -> {
            ChatColor color = i % 2 == 0 ? ChatColor.BLUE : ChatColor.DARK_PURPLE;
            sb.append(color).append(ChatColor.STRIKETHROUGH).append('=');
        };
        IntStream.range(0, 15).forEach(consumer); // Add 15 padding
        HEADER = new FancyMessage().safeAppend(sb.toString()).safeAppend(" &e&lBlur&r ").safeAppend(sb.toString());

        IntStream.range(1, 21).forEach(consumer); // start from 1 to start with different colors, then add 20 =. 
        FOOTER = new FancyMessage().safeAppend(sb.toString());
    }

    public BlurCommands(Blur blur) {
        this.blur = blur;
    }

    @Command(aliases = {"blur"}, desc = "Blur main command.",
        help = "Blur main command.")
    public void blur(CommandSender sender, @Switch('v') boolean version) throws AuthorizationException {
        if (!Blur.isDev(sender)) {
            sender.sendMessage(Colors._darkGreen("This server is powered by ").yellow("Blur").darkGreen(".").toString());
            return;
        }
        if (version) {
            sender.sendMessage("Blur version: " + this.blur.getVersion());
            return;
        }
        HEADER.send(sender);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Blur version", this.blur.getVersion());
        map.put("Current sessions", this.blur.getSessionManager().getBlurSessions().size());
        map.forEach((k, v) -> sender.sendMessage(ChatColor.YELLOW + k + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + v));
        FOOTER.send(sender);
    }
}
