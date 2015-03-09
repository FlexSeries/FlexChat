package me.st28.flexseries.flexchat.utils;

import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.PlayerChatter;
import me.st28.flexseries.flexcore.permissions.PermissionNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class ChatterUtils {

    private ChatterUtils() { }

    public static boolean isChatterAllowed(Chatter chatter, PermissionNode permission) {
        Validate.notNull(chatter, "Chatter cannot be null.");
        Validate.notNull(permission, "Permission cannot be null.");

        CommandSender sender;
        if (chatter instanceof PlayerChatter) {
            sender = ((PlayerChatter) chatter).getPlayer();
        } else {
            sender = Bukkit.getConsoleSender();
        }
        return permission.isAllowed(sender);
    }

}