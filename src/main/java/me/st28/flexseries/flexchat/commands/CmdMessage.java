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
package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.backend.format.FormatManager;
import me.st28.flexseries.flexchat.commands.arguments.ChatterArgument;
import me.st28.flexseries.flexchat.logging.ChatLogHelper;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.command.argument.StringArgument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.PlainMessageReference;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CmdMessage extends FlexCommand<FlexChat> {

    Map<String, String> replies = new HashMap<>();

    public CmdMessage(FlexChat plugin) {
        super(plugin, new CommandDescriptor("message").permission(PermissionNodes.MESSAGE));

        addArgument(new ChatterArgument("player", true));
        addArgument(new StringArgument("message", true, true));
    }

    @Override
    public void handleExecute(CommandContext context) {
        FormatManager formatManager = FlexPlugin.getGlobalModule(FormatManager.class);

        Chatter sender = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(context.getSender());
        Chatter target = context.getGlobalObject("player", Chatter.class);

        String rawMessage = context.getGlobalObject("message", String.class);
        String message = formatManager.formatMessage(sender, rawMessage);

        String senderIdentifier = sender.getIdentifier();
        String targetIdentifier = target.getIdentifier();

        if (sender instanceof Player && !sender.hasPermission(PermissionNodes.BYPASS_IGNORE) && target instanceof ChatterPlayer) {
            final PlayerManager playerManager = FlexPlugin.getGlobalModule(PlayerManager.class);

            PlayerData senderData = playerManager.getPlayerData(((Player) sender).getUniqueId());
            UUID targetUuid = ((ChatterPlayer) target).getUuid();

            // Check to see if the sender has ignored the target
            List<String> senderIgnored = (List<String>) senderData.getCustomData("ignored", List.class);
            if (senderIgnored != null && senderIgnored.contains(targetIdentifier)) {
                // The sender has ignored the target -> prevent messaging
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.cannot_message_player", new ReplacementMap("{NAME}", target.getDisplayName()).getMap()));
            }

            // Check to see if the target has ignored the sender
            List<String> targetIgnored = (List<String>) playerManager.getPlayerData(targetUuid).getCustomData("ignored", List.class);
            if (targetIgnored != null && targetIgnored.contains(senderIdentifier)) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.cannot_message_player", new ReplacementMap("{NAME}", target.getDisplayName()).getMap()));
            }
        }

        // Send message
        sender.sendMessage(new PlainMessageReference(message.replace("{SENDER}", ChatColor.ITALIC + "me").replace("{RECEIVER}", target.getDisplayName())));
        target.sendMessage(new PlainMessageReference((message.replace("{SENDER}", sender.getDisplayName()).replace("{RECEIVER}", ChatColor.ITALIC + "me"))));

        // Set reply reference
        replies.put(senderIdentifier, targetIdentifier);
        replies.put(targetIdentifier, senderIdentifier);

        // Log message
        ChatLogHelper.log(ChatColor.stripColor("[[-MSG-]] " + sender.getName() + " TO " + target.getName() + " > " + rawMessage));
    }


}