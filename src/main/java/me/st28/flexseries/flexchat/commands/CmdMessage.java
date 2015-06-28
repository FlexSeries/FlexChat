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
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.player.PlayerData;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class CmdMessage extends FlexCommand<FlexChat> {

    //TODO: Better way of implementing this
    Map<UUID, UUID> replies = new HashMap<>();

    public CmdMessage(FlexChat plugin) {
        super(
            plugin,
            "message",
            Arrays.asList(new CommandArgument("player", true), new CommandArgument("message", true)),
            new FlexCommandSettings<FlexChat>()
                .description("Send a private message")
                .permission(PermissionNodes.MESSAGE)
                .shouldFixArguments(false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);
        ChatterManagerImpl chatterManager = FlexPlugin.getRegisteredModule(ChatterManagerImpl.class);

        Chatter senderChatter = chatterManager.getChatter(sender);
        String senderIdentifier = senderChatter.getIdentifier();

        Chatter targetChatter;

        if (args[0].equalsIgnoreCase("console")) {
            targetChatter = chatterManager.getChatter(Bukkit.getConsoleSender());
        } else {
            targetChatter = chatterManager.getChatter(CommandUtils.getTargetPlayer(sender, args[0], true));
        }

        String targetIdentifier = targetChatter.getIdentifier();

        if (sender instanceof Player && !PermissionNodes.IGNORE_BYPASS.isAllowed(sender) && targetChatter instanceof ChatterPlayer) {
            PlayerData data = FlexPlugin.getRegisteredModule(PlayerManager.class).getPlayerData(CommandUtils.getSenderUuid(sender));
            UUID uuid = ((ChatterPlayer) targetChatter).getUuid();

            List<String> ignored = (List<String>) data.getCustomData("ignored", List.class);

            ignored.contains(uuid.toString());

            if (ignored.contains(targetIdentifier)) {
                MessageReference.create(FlexChat.class, "errors.ignore_cannot_message", new ReplacementMap("{NAME}", targetChatter.getDisplayName()).getMap());
                return;
            }

            List<String> oIgnored = (List<String>) FlexPlugin.getRegisteredModule(PlayerManager.class).getPlayerData(uuid).getCustomData("ignored", List.class);
            if (oIgnored.contains(senderIdentifier)) {
                MessageReference.create(FlexChat.class, "errors.cannot_message_player", new ReplacementMap("{NAME}", targetChatter.getDisplayName()).getMap());
                return;
            }
        }

        String format = ChatColor.translateAlternateColorCodes('&', channelManager.getMessageFormat());
        String message = ChatFormat.applyApplicableChatColors(senderChatter, StringUtils.stringCollectionToString(Arrays.asList(args).subList(1, args.length)));

        senderChatter.sendMessage(MessageReference.createPlain(format.replace("{SENDER}", ChatColor.ITALIC + "me").replace("{RECEIVER}", targetChatter.getDisplayName()).replace("{MESSAGE}", message)));
        targetChatter.sendMessage(MessageReference.createPlain(format.replace("{SENDER}", senderChatter.getDisplayName()).replace("{RECEIVER}", ChatColor.ITALIC + "me").replace("{MESSAGE}", message)));

        if (sender instanceof Player && targetChatter instanceof ChatterPlayer) {
            UUID senderUuid = ((Player) sender).getUniqueId();
            UUID targetUuid = ((ChatterPlayer) targetChatter).getPlayer().getUniqueId();

            replies.put(senderUuid, targetUuid);
            replies.put(targetUuid, senderUuid);
        }

        //FlexChat.CHAT_LOGGER.log(Level.INFO, ChatColor.stripColor("[[-MSG-]] " + senderChatter.getName() + " TO " + targetChatter.getName() + " > " + message));
    }

}