package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class CmdReply extends FlexCommand<FlexChat> {

    private CmdMessage messageCommand;

    public CmdReply(FlexChat plugin, CmdMessage messageCommand) {
        super(
                plugin,
                "flexreply",
                new FlexCommandSettings<FlexChat>()
                    .permission(PermissionNodes.MESSAGE)
                    .setPlayerOnly(),
                new CommandArgument("message", true)
        );

        this.messageCommand = messageCommand;
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        UUID targetUuid = messageCommand.replies.get(((Player) sender).getUniqueId());

        if (targetUuid == null || Bukkit.getPlayer(targetUuid) == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.message_no_reply"));
        }

        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        Chatter targetChatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(targetPlayer);

        List<String> newArgs = new ArrayList<>();

        newArgs.add(targetPlayer.getName());
        Collections.addAll(newArgs, args);

        messageCommand.runCommand(sender, "message ", "message", newArgs.toArray(new String[newArgs.size()]), parameters);
    }

}