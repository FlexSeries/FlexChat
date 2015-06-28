/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexchat.permissions;

import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum PermissionNodes implements PermissionNode {

    ADMIN,

    /* Dynamic permissions */
    AUTOJOIN,
    CHAT,
    JOIN,
    LEAVE,
    VIEW,
    /* ------------------- */

    VIEW_BYPASS,

    COLOR,
    FORMAT,
    MAGIC,

    SPY_TOGGLE,
    SPY_TOGGLE_OTHER,
    SPY_PERSISTENT,

    INFO,

    IGNORE,
    IGNORE_BYPASS,

    MESSAGE;

    private String node;

    PermissionNodes() {
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