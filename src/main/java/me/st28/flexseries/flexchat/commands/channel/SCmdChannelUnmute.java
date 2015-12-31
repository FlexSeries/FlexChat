package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.Channel;
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
import me.st28.flexseries.flexlib.permission.PermissionNode;

final class SCmdChannelUnmute extends Subcommand<FlexChat> {

    public SCmdChannelUnmute(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("unmute").description("Unmute a channel"));

        addArgument(new ChannelArgument("channel", false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Channel channel = context.getGlobalObject("channel", Channel.class);

        if (!PermissionNode.buildVariableNode(PermissionNodes.MUTE, channel.getName()).isAllowed(context.getSender())) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "unmute").put("{CHANNEL}", channel.getName()).getMap()));
        }

        if (FlexChatAPI.getChannelManager().unmuteChannel(channel)) {
            channel.sendMessage(MessageManager.getMessage(FlexChat.class, "alerts_channels.channel_unmuted",
                    new ReplacementMap("{COLOR}", channel.getColor().toString())
                            .put("{CHANNEL}", channel.getName())
                            .getMap())
            );
        } else {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_unmuted"));
        }
    }

}