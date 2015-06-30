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
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.QuickMap;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class CmdChannel extends FlexCommand<FlexChat> {

    public static Channel matchChannel(String input) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);

        Collection<String> channelNames = StringUtils.collectionToStringList(channelManager.getChannels(), channel -> channel.getName().toLowerCase());

        Channel channel = null;
        String inputName = input.toLowerCase();

        if (channelNames.contains(inputName)) {
            channel = channelManager.getChannel(inputName);
        } else {
            List<String> matched = new ArrayList<>();

            for (String name : channelNames) {
                if (name.startsWith(inputName)) {
                    matched.add(name);
                }
            }

            if (matched.size() > 1) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_multiple_found", new QuickMap<>("{NAME}", inputName).getMap()));
            } else if (matched.size() == 1) {
                channel = channelManager.getChannel(matched.get(0));
            }
        }

        return channel;
    }

    public CmdChannel(FlexChat plugin) {
        super(
                plugin,
                "channel",
                Collections.singletonList(new CommandArgument("channel", true)),
                new FlexCommandSettings<FlexChat>()
                        .description("Quick channel switcher")
                        .defaultSubcommand("list")
                        .description("Channel commands")
        );

        //registerSubcommand(new SCmdChannelInfo(plugin, this));
        registerSubcommand(new SCmdChannelJoin(this));
        registerSubcommand(new SCmdChannelLeave(this));
        registerSubcommand(new SCmdChannelList(this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManagerImpl.class).getChatter(sender);

        Channel channel = matchChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        }

        List<ChannelInstance> instances = channel.getInstances(chatter);

        if (instances == null || instances.isEmpty()) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_cannot_join"));
        } else if (instances.size() == 1) {
            ChannelInstance instance = instances.get(0);

            if (!chatter.isInInstance(instance) && !chatter.hasPermission(PermissionNodes.buildVariableNode(PermissionNodes.JOIN, channel.getName()))) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "join").put("{CHANNEL}", channel.getName()).getMap()));
            }

            if (chatter.addInstance(instance)) {
                instance.sendMessage(MessageReference.create(FlexChat.class, "alerts_channel.chatter_joined", new ReplacementMap("{CHATTER}", chatter.getName()).put("{COLOR}", channel.getColor().toString()).put("{CHANNEL}", channel.getName()).getMap()));
            }

            if (chatter.setActiveInstance(instance)) {
                MessageReference.create(FlexChat.class, "notices.channel_active_set", new ReplacementMap("{COLOR}", channel.getColor().toString()).put("{CHANNEL}", channel.getName()).getMap()).sendTo(sender);
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_active_already_set", new ReplacementMap("{CHANNEL}", channel.getName()).getMap()));
            }
        }
    }

}