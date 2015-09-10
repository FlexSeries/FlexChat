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
package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChannelInstanceArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.*;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.StringConverter;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class SCmdChannelWho extends Subcommand<FlexChat> {

    public SCmdChannelWho(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("who").description("View chatters in a channel").permission(PermissionNodes.WHO));

        addArgument(new ChannelArgument("channel", false));
        addArgument(new ChannelInstanceArgument("instance", false, "channel"));
    }

    @Override
    public void handleExecute(CommandContext context) {
        ChannelInstance instance = context.getGlobalObject("instance", ChannelInstance.class);
        Channel channel = instance.getChannel();

        Chatter sender = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(context.getSender());

        if (!instance.getChatters().contains(sender) && !sender.hasPermission(PermissionNodes.WHO_OTHER)) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_not_joined", new ReplacementMap("{CHANNEL}", channel.getName()).getMap()));
        }

        List<Chatter> chatters = new ArrayList<>(instance.getChatters());

        if (sender instanceof ChatterPlayer) {
            Player player = ((ChatterPlayer) sender).getPlayer();

            Iterator<Chatter> iterator = chatters.iterator();
            while (iterator.hasNext()) {
                Chatter next = iterator.next();

                if (next instanceof ChatterPlayer && !player.canSee(((ChatterPlayer) next).getPlayer())) {
                    iterator.remove();
                }
            }
        }

        List<String> names = chatters.stream().map(Chatter::getName).sorted().collect(Collectors.toList());

        ListBuilder builder = new ListBuilder("subtitle", "Chatters", channel.getName(), context.getLabel());

        if (!names.isEmpty()) {
            StringUtils.collectionToString(names, ChatColor.DARK_GRAY + ", ", new StringConverter<String>() {
                @Override
                public String toString(String string) {
                    return ChatColor.GOLD + string;
                }
            });
        }

        builder.sendTo(context.getSender(), 1);
    }

}