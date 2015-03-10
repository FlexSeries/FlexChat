package me.st28.flexseries.flexchat.commands.chat_spy;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.backend.ChatAdminManager;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class SCmdChatSpyToggle extends FlexCommand<FlexChat> {

    public SCmdChatSpyToggle(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"toggle"},
                parent,
                null,
                new CommandArgument("player", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        boolean isSelf = false;
        Player target;

        if (args.length == 0) {
            target = CommandUtils.getSenderPlayer(sender);
        } else {
            target = CommandUtils.getTargetPlayer(sender, args[0], false);
        }

        isSelf = sender instanceof Player && sender.getName().equalsIgnoreCase(target.getName());

        if (!isSelf) {
            if (!PermissionNodes.CHAT_SPY_TOGGLE_OTHER.isAllowed(sender)) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.no_permission"));
            }
        } else {
            if (!PermissionNodes.CHAT_SPY_TOGGLE.isAllowed(sender)) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.no_permission"));
            }
        }

        String status = FlexPlugin.getRegisteredModule(ChatAdminManager.class).toggleChatSpyMode(target) ? (ChatColor.GREEN + "enabled") : (ChatColor.RED + "disabled");

        if (!isSelf) {
            // Alert player
            MessageReference.create(FlexChat.class, "notices.chat_spy_toggled_other", new ReplacementMap("{STATUS}", status).put("{PLAYER}", target.getName()).getMap()).sendTo(target);
        }

        MessageReference.create(FlexChat.class, "notices.chat_spy_toggled", new ReplacementMap("{STATUS}", status).getMap()).sendTo(sender);
    }

}