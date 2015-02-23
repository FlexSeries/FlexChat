package me.st28.flexseries.flexchat.permissions;

import me.st28.flexseries.flexcore.permissions.PermissionNode;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;

public enum PermissionNodes implements PermissionNode {

    CHANNEL_CHAT,
    CHANNEL_JOIN,
    CHANNEL_LEAVE,
    CHANNEL_VIEW,

    CHAT_COLOR,
    CHAT_FORMAT,
    CHAT_MAGIC,

    MESSAGE;

    private String node;

    private PermissionNodes() {
        node = "flexchat." + toString().toLowerCase().replace("_", ".");
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public boolean isAllowed(Permissible permissible) {
        return permissible.hasPermission(node);
    }

    public static PermissionNode buildVariableNode(PermissionNodes mainPerm, String... variables) {
        final String node = mainPerm.node + "." + StringUtils.stringCollectionToString(Arrays.asList(variables), ".").toLowerCase();
        return new PermissionNode() {
            @Override
            public boolean isAllowed(Permissible permissible) {
                return permissible.hasPermission(node);
            }

            @Override
            public String getNode() {
                return node;
            }
        };
    }

}