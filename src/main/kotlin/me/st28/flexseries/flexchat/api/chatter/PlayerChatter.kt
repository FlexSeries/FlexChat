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
package me.st28.flexseries.flexchat.api.chatter

import me.st28.flexseries.flexchat.backend.VanillaChatProvider
import me.st28.flexseries.flexlib.message.Message
import org.bukkit.entity.Player

class PlayerChatter internal constructor(provider: VanillaChatProvider, val player: Player) :
        Chatter(provider, player.uniqueId.toString())
{

    override val name: String
        get() = player.name

    override val displayName: String
        get() = player.displayName ?: player.name

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun sendMessage(message: Message) {
        message.sendTo(player)
    }

}
