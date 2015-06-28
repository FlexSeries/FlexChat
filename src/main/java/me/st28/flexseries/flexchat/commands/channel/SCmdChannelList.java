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
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.*;
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.QuickMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public final class SCmdChannelList extends FlexSubcommand<FlexChat> {

    public SCmdChannelList(FlexCommand<FlexChat> parent) {
        super(parent, "list", Collections.singletonList(new CommandArgument("page", false)), new FlexCommandSettings().description("List channels"));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        int page = CommandUtils.getPage(args, 0, false);

        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);
        ChatterManagerImpl chatterManager = FlexPlugin.getRegisteredModule(ChatterManagerImpl.class);

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
        boolean canSenderBypass = PermissionNodes.VIEW_BYPASS.isAllowed(sender);

        ListBuilder builder = new ListBuilder("page", "Channels", null, label);

        Set<Channel> chatterChannels = chatter.getInstances().stream().map(ChannelInstance::getChannel).collect(Collectors.toSet());
        for (Channel channel : channels) {
            if (!canSenderBypass && !PermissionNodes.buildVariableNode(PermissionNodes.VIEW, channel.getName()).isAllowed(sender)) {
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
                .put("{ACTIVE}", channel == activeChannel ? channelManager.getActiveSymbol() : "")
                .getMap()
            );
        }

        builder.sendTo(sender, page);
    }

}