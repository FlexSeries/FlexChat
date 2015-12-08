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
package me.st28.flexseries.flexchat.commands.arguments;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ChannelArgument extends Argument {

    public ChannelArgument(String name, boolean isRequired) {
        super(name, isRequired);
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        ChannelManagerImpl channelManager = FlexPlugin.getGlobalModule(ChannelManagerImpl.class);

        Collection<String> channelNames = channelManager.getChannels().stream().map(channel -> channel.getName().toLowerCase()).collect(Collectors.toList());

        Channel channel;
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
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_multiple_found", new ReplacementMap("{NAME}", inputName).getMap()));
            } else if (matched.size() == 1) {
                channel = channelManager.getChannel(matched.get(0));
            } else {
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_not_found", new ReplacementMap("{NAME}", input).getMap()));
            }
        }

        return channel;
    }

    @Override
    public Object getDefaultValue(CommandContext context) {
        Chatter sender = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(context.getSender());

        ChannelInstance instance = sender.getActiveInstance();
        if (instance == null) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_active_not_set"));
        }

        return instance.getChannel();
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String input) {
        CommandSender sender = context.getSender();
        ChannelManagerImpl channelManager = FlexPlugin.getGlobalModule(ChannelManagerImpl.class);

        boolean canSenderBypass = PermissionNodes.BYPASS_VIEW.isAllowed(sender);

        return channelManager.getChannels().stream().filter(new Predicate<Channel>() {
            @Override
            public boolean test(Channel channel) {
                return canSenderBypass || PermissionNode.buildVariableNode(PermissionNodes.VIEW, channel.getName()).isAllowed(sender);
            }
        }).map(Channel::getName).collect(Collectors.toList());
    }

}