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
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChannelInstanceArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.CommandUtils;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

public final class CmdChannel extends FlexCommand<FlexChat> {

    public CmdChannel(FlexChat plugin) {
        super(
                plugin,
                new CommandDescriptor("channel").defaultCommand("list")
        );

        addArgument(new ChannelArgument("channel", true));
        addArgument(new ChannelInstanceArgument("instance", false, "channel"));

        registerSubcommand(new SCmdChannelInfo(this));
        registerSubcommand(new SCmdChannelJoin(this));
        registerSubcommand(new SCmdChannelKick(this));
        registerSubcommand(new SCmdChannelLeave(this));
        registerSubcommand(new SCmdChannelList(this));
        registerSubcommand(new SCmdChannelMute(this));
        registerSubcommand(new SCmdChannelUnmute(this));
        registerSubcommand(new SCmdChannelWho(this));
    }

    @Override
    public void handleExecute(CommandContext context) {
        CommandSender sender = context.getSender();
        Chatter chatter = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(sender);

        Channel channel = context.getGlobalObject("channel", Channel.class);
        ChannelInstance instance = context.getGlobalObject("instance", ChannelInstance.class);

        // Check if the chatter has permission for the channel
        if (!chatter.isInInstance(instance) && !chatter.hasPermission(PermissionNode.buildVariableNode(PermissionNodes.JOIN, channel.getName()))) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "join").put("{CHANNEL}", channel.getName()).getMap()));
        }

        // Check if the chatter has the ability to join instances they normally shouldn't be in
        if (!channel.getInstances(chatter).contains(instance)) {
            // If the channel doesn't normally return the specified instance, the player isn't in
            // the instance normally. Check to make sure player can join any instance
            CommandUtils.performPermissionCheck(context, PermissionNodes.BYPASS_JOIN);
        }

        if (chatter.addInstance(instance)) {
            instance.alertJoin(chatter);
        }

        if (chatter.setActiveInstance(instance)) {
            if (instance.getDisplayName() != null && !context.isDefaultValue("instance")) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.channel_active_set_specific",
                        new ReplacementMap("{COLOR}", channel.getColor().toString())
                                .put("{CHANNEL}", channel.getName())
                                .put("{INSTANCE}", instance.getDisplayName())
                                .getMap()));
            } else {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.channel_active_set",
                        new ReplacementMap("{COLOR}", channel.getColor().toString())
                                .put("{CHANNEL}", channel.getName())
                                .getMap()));
            }
        } else {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_active_already_set", new ReplacementMap("{CHANNEL}", channel.getName()).getMap()));
        }
    }

}