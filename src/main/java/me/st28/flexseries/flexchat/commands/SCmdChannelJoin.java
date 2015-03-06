package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SCmdChannelJoin extends FlexCommand<FlexChat> {

    public SCmdChannelJoin(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"join"},
                parent,
                new FlexCommandSettings<FlexChat>()
                        .description("Joins a channel"),
                new CommandArgument("channel", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(sender);

        Channel channel = CmdChannel.matchChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        } else if (!channel.isJoinableByCommand()) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_cannot_join"));
        }

        chatter.addChannel(channel).sendMessage(sender);
    }

}