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
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.AbstractCommand;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandUtils;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.command.argument.PageArgument;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.QuickMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

// TODO: Show multiple instances of a channel if multiple instances are joined
final class SCmdChannelList extends Subcommand<FlexChat> {

    public SCmdChannelList(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("list").description("List channels").permission(PermissionNodes.LIST));

        addArgument(new ChannelArgument("channel", false));
        addArgument(new PageArgument(false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        if (!context.isDefaultValue("channel")) {
            listInstances(context);
            return;
        }

        CommandSender sender = context.getSender();
        int page = context.getGlobalObject("page", Integer.class);

        ChannelManagerImpl channelManager = FlexPlugin.getGlobalModule(ChannelManagerImpl.class);
        ChatterManagerImpl chatterManager = FlexPlugin.getGlobalModule(ChatterManagerImpl.class);

        Chatter chatter = chatterManager.getChatter(sender);

        /*final Map<Channel, List<ChannelInstance>> instances = new HashMap<>();

        for (ChannelInstance instance : chatter.getInstances()) {
            if (!instances.containsKey(instance.getChannel())) {
                instances.put(instance.getChannel(), new ArrayList<>());
            }
            instances.get(instance.getChannel()).add(instance);
        }*/

        final List<Channel> channels = new ArrayList<>(channelManager.getChannels());
        final Channel activeChannel = chatter.getActiveInstance() == null ? null : chatter.getActiveInstance().getChannel();

        /*
         * SORT ORDER:
         * 1) Active channel
         * 2) Joined channels (alphabetical)
         * 3) Unjoined channels (alphabetical)
         */
        Collections.sort(channels, new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                if (o1 == o2) return 0;

                if (o2 == activeChannel) {
                    // If the other channel is active, it is automatically first.
                    return 1;
                } else if (o1 == activeChannel) {
                    // If the first channel is active, it is automatically first.
                    return -1;
                }

                boolean isJoin1 = channels.contains(o1);
                boolean isJoin2 = channels.contains(o2);

                if (isJoin1 != isJoin2) {
                    // If only one of the channels is joined, the joined one is first.
                    return isJoin1 ? 1 : -1;
                } else {
                    // If both channels are joined or unjoined, they are sorted alphabetically.
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            }
        });

        // Remove channels that shouldn't be visible on the list command.
        boolean canSenderBypass = PermissionNodes.BYPASS_VIEW.isAllowed(sender);

        ListBuilder builder = new ListBuilder("page", "Chat Channels", null, context.getLabel());

        Set<Channel> chatterChannels = chatter.getInstances().stream().map(ChannelInstance::getChannel).collect(Collectors.toSet());
        for (Channel channel : channels) {
            if (!canSenderBypass && !PermissionNode.buildVariableNode(PermissionNodes.VIEW, channel.getName()).isAllowed(sender) && !chatterChannels.contains(channel)) {
                continue;
            }

            String status;
            if (chatterChannels.contains(channel)) {
                status = ChatColor.GREEN + "joined";
            /*} else if (channel.getBanned().contains(identifier)) {
                status = "" + ChatColor.DARK_RED + ChatColor.ITALIC + "banned";*/
            } else {
                status = ChatColor.RED + "not joined";
            }

            builder.addMessage("flexchat_channel", new QuickMap<>("{CHANNEL}", channel.getName())
                            .put("{COLOR}", channel.getColor().toString())
                            .put("{STATUS}", status)
                            .put("{ACTIVE}", channel == activeChannel ? channelManager.getActiveSymbolChannel() : "")
                            .getMap()
            );
        }

        builder.sendTo(sender, page);
    }

    private void listInstances(CommandContext context) {
        CommandUtils.performPermissionCheck(context, PermissionNodes.LIST_INSTANCE);

        CommandSender sender = context.getSender();
        int page = context.getGlobalObject("page", Integer.class);

        ChannelManagerImpl channelManager = FlexPlugin.getGlobalModule(ChannelManagerImpl.class);
        ChatterManagerImpl chatterManager = FlexPlugin.getGlobalModule(ChatterManagerImpl.class);

        Chatter chatter = chatterManager.getChatter(sender);
        Channel channel = context.getGlobalObject("channel", Channel.class);

        List<ChannelInstance> instances = new ArrayList<>();

        if (PermissionNodes.LIST_INSTANCE_ALL.isAllowed(context.getSender())) {
            instances.addAll(channel.getInstances());
        } else {
            instances.addAll(channel.getAllInstances(chatter));
        }

        Collections.sort(instances, new Comparator<ChannelInstance>() {
            @Override
            public int compare(ChannelInstance o1, ChannelInstance o2) {
                if (o1 == o2) return 0;

                if (o1.getDisplayName() == null) {
                    return -1;
                } else if (o2.getDisplayName() == null) {
                    return 1;
                }
                return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
            }
        });

        ListBuilder builder = new ListBuilder("page_subtitle", "Channel Instances", channel.getColor() + channel.getName(), context.getLabel());

        List<ChannelInstance> chatterInstances = chatter.getInstances();
        ChannelInstance activeInstance = chatter.getActiveInstance();

        for (ChannelInstance instance : instances) {
            String status;
            if (chatterInstances.contains(instance)) {
                status = ChatColor.GREEN + "joined";
            } else {
                status = ChatColor.RED + "not joined";
            }

            builder.addMessage("flexchat_channel_instance", new QuickMap<>("{INSTANCE}", instance.getDisplayName() == null ? "(default)" : instance.getDisplayName())
                            .put("{STATUS}", status)
                            .put("{ACTIVE}", instance == activeInstance ? channelManager.getActiveSymbolInstance() : "")
                            .getMap()
            );
        }

        builder.sendTo(context.getSender(), page);
    }

}