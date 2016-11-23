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
import me.st28.flexseries.flexchat.api.ChatManager
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.*

class ChatModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "chat", "Manages chat"), ChatManager {

    internal val providers: MutableMap<String, ChatProvider> = HashMap()

    override fun registerProvider(provider: ChatProvider): Boolean {
        val key = provider.name.toLowerCase()
        if (providers.containsKey(key)) {
            return false
        }

        providers.put(key, provider)

        provider.enable(null)

        if (provider is Listener) {
            // TODO: Make provider have its own plugin instance
            Bukkit.getPluginManager().registerEvents(provider, plugin)
        }

        LogHelper.info(this, "Registered chat provider '$key' (${provider.javaClass.canonicalName})")
        return true
    }

}
