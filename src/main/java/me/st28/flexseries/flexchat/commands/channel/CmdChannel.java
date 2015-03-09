package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.FlexHelpCommand;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import me.st28.flexseries.flexcore.utils.StringConverter;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class CmdChannel extends FlexCommand<FlexChat> {

    public static Channel matchChannel(String input) {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);

        Collection<String> channelNames = StringUtils.collectionToStringList(channelManager.getChannels(), new StringConverter<Channel>() {
            @Override
            public String toString(Channel object) {
                return object.getName().toLowerCase();
            }
        });

        Channel channel = null;
        String inputName = input.toLowerCase();

        if (channelNames.contains(inputName)) {
            channel = channelManager.getChannelByName(inputName);
        } else {
            List<String> matched = new ArrayList<>();

            for (String name : channelNames) {
                if (name.startsWith(inputName)) {
                    matched.add(name);
                }
            }

            if (matched.size() > 1) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_multiple_found", new QuickMap<>("{NAME}", inputName).getMap()));
            } else if (matched.size() == 1) {
                channel = channelManager.getChannelByName(matched.get(0));
            }
        }

        return channel;
    }

    public CmdChannel(FlexChat plugin) {
        super(
                plugin,
                "flexchannel",
                new FlexCommandSettings<FlexChat>()
                        .description("Quick channel switcher")
                        .defaultSubcommand("list")
                        .helpPath("FlexChat.Channels")
                        .description("Channel commands"),
                new CommandArgument("channel", true)
        );


        registerSubcommand(new SCmdChannelInfo(plugin, this));
        registerSubcommand(new SCmdChannelJoin(plugin, this));
        registerSubcommand(new SCmdChannelLeave(plugin, this));
        registerSubcommand(new SCmdChannelList(plugin, this));
        registerSubcommand(new FlexHelpCommand<>(plugin, new String[]{"help"}, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(sender);

        Channel channel = matchChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        }

        chatter.setActiveChannel(channel).sendMessage(sender);
    }

}