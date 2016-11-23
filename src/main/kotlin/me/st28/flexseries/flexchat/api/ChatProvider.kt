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

import me.st28.flexseries.flexlib.util.translateColorCodes
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * Represents something that provides chat messages for a [Channel].
 */
abstract class ChatProvider(val plugin: JavaPlugin, val name: String) {

    /**
     * The display name of the provider, including color codes.
     */
    var displayName: String? = null
        get() = field ?: name
        private set

    /**
     * The chatters from this provider.
     */
    internal val chatters: MutableMap<String, Chatter> = HashMap()

    internal fun loadGeneralConfig(config: ConfigurationSection?) {
        displayName = config?.getString("display name")?.translateColorCodes()
    }

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
    open fun disable() {
        chatters.values.forEach { FlexChatAPI.chatters.saveChatter(it) }
    }

    fun getChatters(): Collection<Chatter> {
        return chatters.values
    }

    protected fun loadChatter(identifier: String): Chatter {
        val chatter = FlexChatAPI.chatters.loadChatter(this, identifier)
        chatters.put(identifier, chatter)
        return chatter
    }

    protected fun unloadChatter(identifier: String): Chatter? {
        val chatter = chatters.remove(identifier)
        if (chatter != null) {
            FlexChatAPI.chatters.unloadChatter(chatter)
        }
        return chatter
    }

    /**
     * Sends a message to a chatter registered under this provider.
     */
    abstract fun sendMessage(chatter: Chatter, message: String)

}
