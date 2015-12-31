/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexchat.permissions;

import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum PermissionNodes implements PermissionNode {

    ADMIN,

    /* Dynamic permissions */
    AUTOJOIN,
    CHAT,
    INFO,
    JOIN,
    LEAVE,
    MUTE,
    VIEW,
    /* ------------------- */

    BYPASS_IGNORE,
    BYPASS_JOIN,
    BYPASS_MUTE,
    BYPASS_VIEW,

    KICK,

    COLOR,
    FORMAT,
    MAGIC,

    SPY, // Also dynamic
    SPY_PERSISTENT,

    LIST,
    LIST_INSTANCE,
    LIST_INSTANCE_ALL,

    IGNORE,

    MESSAGE,

    WHO,
    WHO_OTHER;

    private String node;

    PermissionNodes() {
        node = "flexchat." + toString().toLowerCase().replace("_", ".");
    }

    @Override
    public String getNode() {
        return node;
    }

}