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
import me.st28.flexseries.flexlib.player.data.PlayerData;
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

        String message = formatManager.formatMessage(sender, context.getGlobalObject("message", String.class));

        String senderIdentifier = sender.getIdentifier();
        String targetIdentifier = target.getIdentifier();

        if (sender instanceof Player && !sender.hasPermission(PermissionNodes.IGNORE_BYPASS) && target instanceof ChatterPlayer) {
            final PlayerManager playerManager = FlexPlugin.getGlobalModule(PlayerManager.class);

            PlayerData data = playerManager.getPlayerData(((Player) sender).getUniqueId());
            UUID uuid = ((ChatterPlayer) target).getUuid();

            List<String> ignored = (List<String>) data.getCustomData("ignored", List.class);

            if (ignored != null) {
                if (ignored.contains(targetIdentifier)) {
                    throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.cannot_message_player", new ReplacementMap("{NAME}", target.getDisplayName()).getMap()));
                }

                List<String> oIgnored = (List<String>) playerManager.getPlayerData(uuid).getCustomData("ignored", List.class);
                if (oIgnored != null && oIgnored.contains(senderIdentifier)) {
                    throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.cannot_message_player", new ReplacementMap("{NAME}", target.getDisplayName()).getMap()));
                }
            }
        }

        // Send message
        sender.sendMessage(new PlainMessageReference(message.replace("{SENDER}", ChatColor.ITALIC + "me").replace("{RECEIVER}", target.getDisplayName())));
        target.sendMessage(new PlainMessageReference((message.replace("{SENDER}", sender.getDisplayName()).replace("{RECEIVER}", ChatColor.ITALIC + "me"))));

        // Set reply reference
        replies.put(senderIdentifier, targetIdentifier);
        replies.put(targetIdentifier, senderIdentifier);

        // Log message
        ChatLogHelper.log(ChatColor.stripColor("[[-MSG-]] " + sender.getName() + " TO " + target.getName() + " > " + message));
    }


}