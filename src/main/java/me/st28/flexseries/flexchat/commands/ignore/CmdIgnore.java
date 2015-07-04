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
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.player.PlayerData;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CmdIgnore extends FlexCommand<FlexChat> {

    public CmdIgnore(FlexChat plugin) {
        super(plugin, "ignore", Collections.singletonList(new CommandArgument("player", true)), new FlexCommandSettings<>()
            .permission(PermissionNodes.IGNORE)
            .setPlayerOnly(true)
        );

        registerSubcommand(new SCmdIgnoreList(this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        Player target = CommandUtils.getTargetPlayer(sender, args[0], true);

        PlayerData data = FlexPlugin.getRegisteredModule(PlayerManager.class).getPlayerData(CommandUtils.getSenderUuid(sender));
        List<String> ignored = data.getCustomData("ignored", List.class);
        if (ignored == null) {
            ignored = new ArrayList<>();
            data.setCustomData("ignored", ignored);
        }

        String targetIdentifier = target.getUniqueId().toString();

        if (ignored.contains(targetIdentifier)) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.ignore_exists", new ReplacementMap("{NAME}", target.getName()).getMap()));
        } else if (PermissionNodes.IGNORE_BYPASS.isAllowed(target)) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.ignore_not_allowed", new ReplacementMap("{NAME}", target.getName()).getMap()));
        }

        ignored.add(targetIdentifier);
        MessageReference.create(FlexChat.class, "notices.ignore_added", new ReplacementMap("{NAME}", target.getName()).getMap()).sendTo(sender);
    }

}