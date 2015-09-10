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
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
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

        List<ChannelInstance> instances = channel.getInstances(sender);
        if (instances.size() == 1) {
            return instances.get(0);
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