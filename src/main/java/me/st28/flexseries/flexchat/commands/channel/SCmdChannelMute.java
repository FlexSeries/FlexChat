package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.AbstractCommand;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.command.argument.TimeArgument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.utils.QuickMap;
import me.st28.flexseries.flexlib.utils.TimeUtils;

final class SCmdChannelMute extends Subcommand<FlexChat> {

    public SCmdChannelMute(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("mute").description("Mute a channel"));

        addArgument(new ChannelArgument("channel", false));
        addArgument(new TimeArgument("time", false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Channel channel = context.getGlobalObject("channel", Channel.class);

        if (!PermissionNode.buildVariableNode(PermissionNodes.MUTE, channel.getName()).isAllowed(context.getSender())) {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "mute").put("{CHANNEL}", channel.getName()).getMap()));
        }

        int time;
        if (context.isDefaultValue("time")) {
            time = -1;
        } else {
            time = context.getGlobalObject("time", Integer.class);
        }

        if (FlexChatAPI.getChannelManager().muteChannel(channel, time)) {
            QuickMap<String, Object> replacements = new ReplacementMap("{COLOR}", channel.getColor().toString())
                    .put("{CHANNEL}", channel.getName());

            String message;
            if (time <= 0) {
                message = "alerts_channels.channel_muted";
            } else {
                message = "alerts_channels.channel_muted_time";

                replacements.put("{TIME}", TimeUtils.formatSeconds(time));
            }

            channel.sendMessage(MessageManager.getMessage(FlexChat.class, message, replacements.getMap()));
        } else {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_muted"));
        }
    }

}