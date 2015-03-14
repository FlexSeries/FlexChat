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

public final class CmdUnignore extends FlexCommand<FlexChat> {

    public CmdUnignore(FlexChat plugin) {
        super(
                plugin,
                "flexunignore",
                new FlexCommandSettings<FlexChat>()
                    .permission(PermissionNodes.IGNORE),
                new CommandArgument("player", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);

        Chatter senderChatter = chatterManager.getChatter(sender);
        Chatter targetChatter = chatterManager.getChatter(CommandUtils.getTargetPlayer(sender, args[0], true));

        senderChatter.removeIgnored(targetChatter).sendMessage(sender);
    }

}