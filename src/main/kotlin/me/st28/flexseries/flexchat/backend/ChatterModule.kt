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
package me.st28.flexseries.flexchat.backend

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.ChatterManager
import me.st28.flexseries.flexlib.plugin.FlexModule
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class ChatterModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "chatters", "Manages chatters"), ChatterManager {

    internal val chatters: MutableMap<String, Chatter> = HashMap()

    override fun registerChatter(chatter: Chatter) {
        chatters.put(chatter.identifier, chatter)
    }

    override fun saveChatter(chatter: Chatter) {
    }

    override fun unregisterChatter(chatter: Chatter) {
        chatters.remove(chatter.identifier)
    }

    override fun getChatter(identifier: String): Chatter? {
        return chatters[identifier]
    }

    override fun getChatter(sender: CommandSender): Chatter {
        return chatters[(sender as? Player)?.uniqueId?.toString() ?: "CONSOLE"]!!
    }

}
