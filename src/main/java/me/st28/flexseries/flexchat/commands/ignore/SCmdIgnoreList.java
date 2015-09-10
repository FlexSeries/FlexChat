/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexchat.commands.ignore;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SCmdIgnoreList extends Subcommand<FlexChat> {

    public SCmdIgnoreList(FlexCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("list").playerOnly(true).description("List ignored players"));
    }

    @Override
    public void handleExecute(CommandContext context) {
        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);

        List<String> ignored = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(((Player) context.getSender()).getUniqueId()).getCustomData("ignored", List.class);
        if (ignored == null) {
            ignored = new ArrayList<>();
        }

        List<String> names = new ArrayList<>();
        for (String identifier : ignored) {
            UUID uuid;
            try {
                uuid = UUID.fromString(identifier);
            } catch (Exception ex) {
                names.add(identifier);
                continue;
            }

            names.add(uuidTracker.getLatestName(uuid));
        }

        Collections.sort(names);

        String namesFormatted = StringUtils.collectionToString(names, ChatColor.GRAY + ", ");

        ListBuilder builder = new ListBuilder("title", "Ignored", null, context.getLabel());
        if (!names.isEmpty()) {
            builder.addMessage(namesFormatted);
        }
        builder.sendTo(context.getSender(), 1);
    }

}