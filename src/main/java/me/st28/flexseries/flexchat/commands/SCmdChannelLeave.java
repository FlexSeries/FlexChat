package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.command.CommandSender;

public class SCmdChannelLeave extends FlexCommand<FlexChat> {

    public SCmdChannelLeave(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"leave"},
                parent,
                new FlexCommandSettings<FlexChat>()
                        .description("Leaves currently active channel")
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);
        Chatter chatter = channelManager.getChatter(sender);
        Channel activeChannel = chatter.getActiveChannel();

        if (activeChannel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_none"));
        } else if (!activeChannel.isLeaveableByCommand()) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_cannot_leave"));
        }

        channelManager.removeChatterFromChannel(activeChannel, chatter).sendMessage(sender);
    }

}