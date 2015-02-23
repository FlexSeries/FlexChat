package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.FlexHelpCommand;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.command.CommandSender;

public final class CmdChannel extends FlexCommand<FlexChat> {

    public CmdChannel(FlexChat plugin) {
        super(
                plugin,
                new String[]{"flexchannel", "channel", "channels", "ch"},
                null,
                new FlexCommandSettings<FlexChat>()
                        .description("Quick channel switcher")
                        .defaultSubcommand("list")
                        .helpPath("FlexChat.Channels")
                        .helpDescription("Channel commands"),
                new CommandArgument("channel", true)
        );


        registerSubcommand(new SCmdChannelInfo(plugin, this));
        registerSubcommand(new SCmdChannelJoin(plugin, this));
        registerSubcommand(new SCmdChannelLeave(plugin, this));
        registerSubcommand(new SCmdChannelList(plugin, this));
        registerSubcommand(new FlexHelpCommand<>(plugin, new String[]{"help"}, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);
        Chatter chatter = channelManager.getChatter(sender);

        Channel channel = channelManager.getChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        }

        chatter.setActiveChannel(channel).sendMessage(sender);
    }

}