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
import me.st28.flexseries.flexchat.backend.ChatAdminManager;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexSubcommand;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public final class SCmdChatSpyToggle extends FlexSubcommand<FlexChat> {

    public SCmdChatSpyToggle(FlexCommand<FlexChat> parent) {
        super(
                parent,
                "toggle",
                Collections.singletonList(new CommandArgument("player", false)),
                null
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        boolean isSelf;
        Player target;

        if (args.length == 0) {
            target = CommandUtils.getSenderPlayer(sender);
        } else {
            target = CommandUtils.getTargetPlayer(sender, args[0], false);
        }

        isSelf = sender instanceof Player && sender.getName().equalsIgnoreCase(target.getName());

        if (!isSelf) {
            if (!PermissionNodes.SPY_TOGGLE_OTHER.isAllowed(sender)) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.no_permission"));
            }
        } else {
            if (!PermissionNodes.SPY_TOGGLE.isAllowed(sender)) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.no_permission"));
            }
        }

        String status = FlexPlugin.getRegisteredModule(ChatAdminManager.class).toggleChatSpyMode(target) ? (ChatColor.GREEN + "enabled") : (ChatColor.RED + "disabled");

        if (!isSelf) {
            // Alert player
            MessageReference.create(FlexChat.class, "notices.chat_spy_toggled_other", new ReplacementMap("{STATUS}", status).put("{PLAYER}", target.getName()).getMap()).sendTo(target);
        }

        MessageReference.create(FlexChat.class, "notices.chat_spy_toggled", new ReplacementMap("{STATUS}", status).getMap()).sendTo(sender);
    }

}