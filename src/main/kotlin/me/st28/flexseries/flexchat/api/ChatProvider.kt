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
package me.st28.flexseries.flexchat.api

import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.api.chatter.Chatter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * Acts as a layer between a message and chatter provider.
 */
abstract class ChatProvider(val plugin: JavaPlugin, val name: String) {

    protected val chatVariables: MutableMap<String, (Chatter, ChannelInstance) -> String?> = HashMap()

    /**
     * Enables the provider.
     *
     * @param config The configuration for the provider.
     */
    abstract fun enable(config: ConfigurationSection?)

    /**
     * Reloads the provider.
     *
     * @param config The configuration for the provider.
     */
    abstract fun reload(config: ConfigurationSection?)

    /**
     * Disables the provider.
     */
    open fun disable() { }

    /**
     * Registers a chatter with the provider and FlexChat.
     */
    protected fun registerChatter(chatter: Chatter) {
        FlexChatAPI.chatters.registerChatter(chatter)
    }

    /**
     * Convenience method for retrieving a chatter by their identifier.
     */
    protected fun getChatter(identifier: String): Chatter? {
        return FlexChatAPI.chatters.getChatter(identifier)
    }

    /**
     * Unregisters a chatter with the provider and FlexChat.
     */
    protected fun unregisterChatter(identifier: String): Chatter? {
        val found = FlexChatAPI.chatters.getChatter(identifier) ?: return null
        FlexChatAPI.chatters.unregisterChatter(found)
        return found
    }

    /**
     * Returns the chat format for a specified chatter.
     * If this method returns null, the default chat format from the chat manager will be used.
     *
     * @param chatter The [Chatter] requesting the format.
     * @param instance The [ChannelInstance] the chatter is chatting in.
     */
    open fun getChatFormat(chatter: Chatter, instance: ChannelInstance): String? {
        return null
    }

    /**
     * Processes a message by performing variable replacements where applicable.
     * Chat variables should be preferred over overriding this method where possible.
     */
    open fun processFormat(chatter: Chatter, instance: ChannelInstance, format: String): String {
        var ret = format
        chatVariables.forEach {
            if (ret.contains(it.key)) {
                ret = ret.replace(it.key, it.value.invoke(chatter, instance) ?: "")
            }
        }
        return ret
    }

}
