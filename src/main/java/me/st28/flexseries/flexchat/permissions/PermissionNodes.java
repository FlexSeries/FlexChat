package me.st28.flexseries.flexchat.permissions;

import me.st28.flexseries.flexcore.permissions.PermissionNode;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum PermissionNodes implements PermissionNode {

    CHANNEL_CHAT,
    CHANNEL_JOIN,
    CHANNEL_LEAVE,
    CHANNEL_VIEW,
    CHANNEL_VIEW_BYPASS,

    CHAT_COLOR,
    CHAT_FORMAT,
    CHAT_MAGIC,

    CHAT_SPY_TOGGLE,
    CHAT_SPY_TOGGLE_OTHER,
    CHAT_SPY_PERSISTENT,

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

        if (VARIABLE_NODES.containsKey(node)) {
            return VARIABLE_NODES.get(node);
        }

        PermissionNode newNode = new PermissionNode() {
            @Override
            public boolean isAllowed(Permissible permissible) {
                return permissible.hasPermission(node);
            }

            @Override
            public String getNode() {
                return node;
            }
        };

        VARIABLE_NODES.put(node, newNode);
        return newNode;
    }

    private final static Map<String, PermissionNode> VARIABLE_NODES = new HashMap<>();

}