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
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelInstanceArgument extends Argument {

    private String channelArgName;

    public ChannelInstanceArgument(String name, boolean isRequired, String channelArgName) {
        super(name, isRequired);

        Validate.notNull(channelArgName, "Channel argument name cannot be null.");
        this.channelArgName = channelArgName;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        Channel channel = context.getGlobalObject(channelArgName, Channel.class);
        if (channel == null) {
            throw new IllegalStateException("A channel must be specified.");
        }

        final ChannelInstance instance = channel.getInstance(input);

        if (instance == null) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_instance_not_found", new ReplacementMap("{NAME}", input).put("{CHANNEL}", channel.getName()).getMap()));
        }

        return instance;
    }

    @Override
    public Object getDefaultValue(CommandContext context) {
        Channel channel = context.getGlobalObject(channelArgName, Channel.class);
        if (channel == null) {
            return null;
        }

        Chatter sender = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(context.getSender());

        ChannelInstance instance = sender.getActiveInstance();

        if (instance != null && instance.getChannel() == channel) {
            return instance;
        }

        Collection<ChannelInstance> instances = channel.getInstances(sender);

        if (instances == null || instances.isEmpty()) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_cannot_join"));
        } else if (instances.size() == 1) {
            return instances.iterator().next();
        } else {
            // An instance must be specified.
            throw new CommandInterruptedException(InterruptReason.INVALID_USAGE);
        }
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String input) {
        Channel channel = context.getGlobalObject(channelArgName, Channel.class);
        if (channel == null) {
            return new ArrayList<>();
        }

        // TODO: Only show instances that the sender has access to.

        return channel.getInstances().stream().map(ChannelInstance::getDisplayName).collect(Collectors.toList());
    }

}