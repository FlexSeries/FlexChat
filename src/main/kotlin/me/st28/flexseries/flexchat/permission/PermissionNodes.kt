/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexchat.permission

import me.st28.flexseries.flexlib.permission.PermissionNode

enum class PermissionNodes : PermissionNode {

    COLOR,
    FORMAT,
    MAGIC,

    AUTOJOIN,
    INFO,
    JOIN,
    LEAVE,
    CHAT,
    VIEW,
    SPY,

    BYPASS_IGNORE,
    BYPASS_MUTE;

    override val node: String
        get() = "flexchat.${toString().toLowerCase().replace("_", ".")}"

}