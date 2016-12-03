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

import me.st28.flexseries.flexchat.api.FlexChatAPI
import org.bukkit.command.CommandSender

/**
 * Represents the API layer of the chatter manager.
 */
interface ChatterManager {

    /**
     * Registers a chatter and loads its data.
     *
     * Chatter identifiers must be unique.
     */
    fun registerChatter(chatter: Chatter)

    /**
     * Saves a [Chatter].
     */
    fun saveChatter(chatter: Chatter)

    /**
     * Unregisters a chatter.
     */
    fun unregisterChatter(chatter: Chatter)

    /**
     * @return A [Chatter] instance matching the given identifier.
     *         Null if no match was found.
     */
    fun getChatter(identifier: String): Chatter?

    /**
     * @return A [Chatter] instance matching the given name.
     *         Null if no match was found.
     */
    fun getChatterByName(name: String): Chatter?

    /**
     * @return The [Chatter] instance for a CommandSender.
     */
    fun getChatter(sender: CommandSender): Chatter

}

val <T : CommandSender> T.chatter: Chatter
    get() = FlexChatAPI.chatters.getChatter(this)
