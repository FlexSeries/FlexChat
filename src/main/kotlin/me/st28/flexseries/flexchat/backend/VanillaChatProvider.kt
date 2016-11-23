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
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexchat.api.Chatter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles in-game chat.
 */
class VanillaChatProvider(plugin: FlexChat) : ChatProvider(plugin, "vanilla"), Listener {

    override fun enable(config: ConfigurationSection?) { }

    override fun reload(config: ConfigurationSection?) { }

    override fun sendMessage(chatter: Chatter, message: String) {
        chatter.data.get<Player>("player")?.sendMessage(message)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val chatter = loadChatter(e.player.uniqueId.toString())
        chatter.data.set("player", e.player)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val chatter = unloadChatter(e.player.uniqueId.toString())
            ?: return

        chatter.data.remove("player")
    }

    @EventHandler
    fun onAsyncPlayerChat(e: AsyncPlayerChatEvent) {

    }

}
