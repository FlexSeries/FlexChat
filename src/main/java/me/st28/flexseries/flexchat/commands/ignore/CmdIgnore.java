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
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.command.argument.PlayerArgument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CmdIgnore extends FlexCommand<FlexChat> {

    public CmdIgnore(FlexChat plugin) {
        super(plugin, new CommandDescriptor("ignore").permission(PermissionNodes.IGNORE).playerOnly(true).defaultCommand("list"));

        addArgument(new PlayerArgument("player", true).notSender(true).onlineOnly(true));

        registerSubcommand(new SCmdIgnoreList(this));
    }

    @Override
    public void handleExecute(CommandContext context) {
        PlayerReference target = context.getGlobalObject("player", PlayerReference.class);

        PlayerData data = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(((Player) context.getSender()).getUniqueId());
        List<String> ignored = data.getCustomData("ignored", List.class);
        if (ignored == null) {
            ignored = new ArrayList<>();
            data.setCustomData("ignored", ignored);
        }

        String targetIdentifier = target.getUuid().toString();

        if (ignored.contains(targetIdentifier)) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.ignore_exists", new ReplacementMap("{NAME}", target.getName()).getMap()));
        } else if (PermissionNodes.BYPASS_IGNORE.isAllowed(target.getPlayer())) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.ignore_not_allowed", new ReplacementMap("{NAME}", target.getName()).getMap()));
        }

        ignored.add(targetIdentifier);
        throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.ignore_added", new ReplacementMap("{NAME}", target.getName()).getMap()));
    }

}