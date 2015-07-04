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
package me.st28.flexseries.flexchat.commands.chatspy;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.chatadmin.SpySettings;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.FlexSubcommand;
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SCmdChatSpyList extends FlexSubcommand<FlexChat> {

    public SCmdChatSpyList(FlexCommand<FlexChat> parent) {
        super(parent, "list", null, new FlexCommandSettings<>()
            .permission(PermissionNodes.SPY)
            .description("Lists the channels you're spying on")
            .setPlayerOnly(true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        SpySettings settings = FlexPlugin.getRegisteredModule(ChatAdminManager.class).getSpySettings(CommandUtils.getSenderUuid(sender));

        int page = CommandUtils.getPage(args, 0, false);

        ListBuilder builder = new ListBuilder("subtitle", "Chat Spy", settings.isEnabled() ? (ChatColor.GREEN + "Enabled") : (ChatColor.RED + "Disabled"), label);

        builder.sendTo(sender, page);
    }

}