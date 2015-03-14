package me.st28.flexseries.flexchat.commands.ignore;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CmdIgnore extends FlexCommand<FlexChat> {

    public CmdIgnore(FlexChat plugin) {
        super(
                plugin,
                "flexignore",
                new FlexCommandSettings<FlexChat>()
                    .defaultSubcommand("list")
                    .permission(PermissionNodes.IGNORE),
                new CommandArgument("player", true)
        );

        registerSubcommand(new SCmdIgnoreList(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);

        Chatter senderChatter = chatterManager.getChatter(sender);
        Chatter targetChatter = chatterManager.getChatter(CommandUtils.getTargetPlayer(sender, args[0], true));

        senderChatter.addIgnored(targetChatter).sendMessage(sender);
    }

}