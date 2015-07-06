package me.st28.flexseries.flexchat.commands.chatspy;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.chatadmin.SpySettings;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.*;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;

public final class SCmdChatSpyRemove extends FlexSubcommand<FlexChat> {

    public SCmdChatSpyRemove(FlexCommand<FlexChat> parent) {
        super(parent, "remove", Arrays.asList(new CommandArgument("channel", true), new CommandArgument("instance", false)),
            new FlexCommandSettings<FlexChat>()
                .permission(PermissionNodes.SPY)
                .description("Remove a channel or channel instance from your spying list")
                .setPlayerOnly(true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);

        Channel channel = channelManager.getChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new ReplacementMap("{NAME}", args[0]).getMap()));
        }

        ChannelInstance instance = null;
        if (args.length > 1) {
            instance = channel.getInstance(args[1]);

            if (instance == null) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_instance_not_found", new ReplacementMap("{CHANNEL}", channel.getName()).put("{NAME}", args[1]).getMap()));
            }
        }

        SpySettings settings = FlexPlugin.getRegisteredModule(ChatAdminManager.class).getSpySettings(CommandUtils.getSenderUuid(sender));

        if (instance != null) {
            Map<String, String> map = new ReplacementMap("{INSTANCE}", instance.getDisplayName()).put("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeInstance(instance)) {
                MessageReference.create(FlexChat.class, "notices.spy_instance_removed", map).sendTo(sender);
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.spy_instance_already_removed", map));
            }
        } else {
            Map<String, String> map = new ReplacementMap("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeChannel(channel)) {
                MessageReference.create(FlexChat.class, "notices.spy_channel_removed", map).sendTo(sender);
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.spy_channel_already_removed", map));
            }
        }
    }

}