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
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.*;
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.player.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class SCmdIgnoreList extends FlexSubcommand<FlexChat> {

    public SCmdIgnoreList(FlexCommand<FlexChat> parent) {
        super(parent, "list", null, new FlexCommandSettings<>()
            .setPlayerOnly(true)
            .permission(PermissionNodes.IGNORE)
            .description("List all ignored players")
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        PlayerUuidTracker uuidTracker = FlexPlugin.getRegisteredModule(PlayerUuidTracker.class);

        List<String> ignored = FlexPlugin.getRegisteredModule(PlayerManager.class).getPlayerData(CommandUtils.getSenderUuid(sender)).getCustomData("ignored", List.class);
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

            names.add(uuidTracker.getName(uuid));
        }

        Collections.sort(names);

        String namesFormatted = StringUtils.stringCollectionToString(names, ChatColor.GRAY + ", ");

        ListBuilder builder = new ListBuilder("title", "Ignored", null, label);
        if (!names.isEmpty()) {
            builder.addMessage(namesFormatted);
        }
        builder.sendTo(sender, 1);
    }

}