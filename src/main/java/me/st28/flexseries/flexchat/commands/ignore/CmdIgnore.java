/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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