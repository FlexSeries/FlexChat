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
package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChannelInstanceArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.AbstractCommand;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.StringConverter;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

                if (next != sender && next instanceof ChatterPlayer && !player.canSee(((ChatterPlayer) next).getPlayer())) {
                    iterator.remove();
                }
            }
        }

        List<String> names = chatters.stream().map(Chatter::getName).sorted().collect(Collectors.toList());

        ListBuilder builder = new ListBuilder("subtitle", "Chatters", channel.getName(), context.getLabel());

        if (!names.isEmpty()) {
            builder.addMessage(StringUtils.collectionToString(names, ChatColor.DARK_GRAY + ", ", new StringConverter<String>() {
                @Override
                public String toString(String string) {
                    return ChatColor.GOLD + string;
                }
            }));
        }

        builder.sendTo(context.getSender(), 1);
    }

}