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

package com.blurengine.blur;

import com.blurengine.blur.session.BlurPlayer;
import com.supaham.commons.bukkit.CommonPlugin;
import com.supaham.commons.bukkit.players.BukkitPlayerManager;
import com.supaham.commons.bukkit.players.PlayerCreationException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

class BlurPlayerManager extends BukkitPlayerManager<BlurPlayer> {

    public BlurPlayerManager(@Nonnull CommonPlugin plugin) {
        super(plugin, BlurPlayer.class);
    }

    @Override
    protected BlurPlayer createPlayer(Player player) throws PlayerCreationException {
        getPlugin().getLog().finer("Creating %s instance for %s ", BlurPlayer.class, player.getName());
        return super.createPlayer(player);
    }
}
