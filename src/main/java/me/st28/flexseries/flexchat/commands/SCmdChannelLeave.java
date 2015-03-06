package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.Map;

//TODO: Leave specified channel
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
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> replacements) {
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);
        Chatter chatter = chatterManager.getChatter(sender);
        Channel activeChannel = chatter.getActiveChannel();

        if (activeChannel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_none"));
        } else if (!activeChannel.isLeaveableByCommand()) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_cannot_leave"));
        }

        chatter.removeChannel(activeChannel).sendMessage(sender);
    }

}