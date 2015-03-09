package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;

public final class CmdMessage extends FlexCommand<FlexChat> {

    public CmdMessage(FlexChat plugin) {
        super(
                plugin,
                "flexmessage",
                new FlexCommandSettings<FlexChat>()
                        .description("Private message command")
                        .helpPath("FlexChat.Channels")
                        .permission(PermissionNodes.MESSAGE),
                new CommandArgument("player", true),
                new CommandArgument("message", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);

        Chatter senderChatter = chatterManager.getChatter(sender);

        Chatter targetChatter = null;

        if (args[0].equalsIgnoreCase("console")) {
            targetChatter = chatterManager.getChatter(Bukkit.getConsoleSender());
        } else {
            targetChatter = chatterManager.getChatter(CommandUtils.getTargetPlayer(sender, args[0], true));
        }

        String format = ChatColor.translateAlternateColorCodes('&', FlexPlugin.getRegisteredModule(ChannelManager.class).getPrivateMessageFormat());
        String message = ChannelManager.applyApplicableChatColors(sender, StringUtils.stringCollectionToString(Arrays.asList(args).subList(1, args.length)));

        senderChatter.sendMessage(MessageReference.createPlain(format.replace("{SENDER}", ChatColor.ITALIC + "me").replace("{RECEIVER}", targetChatter.getDisplayName()).replace("{MESSAGE}", message)));
        targetChatter.sendMessage(MessageReference.createPlain(format.replace("{SENDER}", senderChatter.getDisplayName()).replace("{RECEIVER}", ChatColor.ITALIC + "me").replace("{MESSAGE}", message)));

        FlexChat.CHAT_LOGGER.log(Level.INFO, ChatColor.stripColor("[[-MSG-]] " + senderChatter.getName() + " TO " + targetChatter.getName() + " > " + message));
    }

}