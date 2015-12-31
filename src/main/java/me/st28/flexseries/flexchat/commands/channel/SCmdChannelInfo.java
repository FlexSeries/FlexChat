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
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelManager;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
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
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.TimeUtils;

import java.util.Map;

final class SCmdChannelInfo extends Subcommand<FlexChat> {

    public SCmdChannelInfo(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("info").description("View information about a channel"));

        addArgument(new ChannelArgument("channel", true));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Channel channel = context.getGlobalObject("channel", Channel.class);

        if (!PermissionNode.buildVariableNode(PermissionNodes.INFO, channel.getName()).isAllowed(context.getSender())) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "view info for").put("{CHANNEL}", channel.getName()).getMap()));
        }

        ListBuilder builder = new ListBuilder("subtitle", "Channel Info", channel.getName(), context.getLabel());

        String description = channel.getDescription();
        if (description == null) {
            description = FlexPlugin.getGlobalModule(ChannelManagerImpl.class).getDefaultChannelDescription();
        }

        builder.addMessage("title", "Description", description);
        builder.addMessage("title", "Range", channel.getRadius() == 0 ? "Global" : Integer.toString(channel.getRadius()));

        ChannelManager channelManager = FlexChatAPI.getChannelManager();
        if (channelManager.isChannelMuted(channel)) {
            int time = channelManager.getChannelMuteTime(channel);
            builder.addMessage("title", "Muted", time < 0 ? "Indefinite" : TimeUtils.formatSeconds(time, true));
        }

        Map<String, String> custom = channel.getCustomInfo();
        if (custom != null && !custom.isEmpty()) {
            custom.entrySet().stream().forEach((entry -> builder.addMessage("title", entry.getKey(), entry.getValue())));
        }

        builder.sendTo(context.getSender());
    }

}