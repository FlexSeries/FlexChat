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
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexlib.message.Message
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ExecutionException

/**
 * Handles in-game chat.
 *
 * This is the default provider, so it is always loaded.
 */
class VanillaChatProvider(plugin: FlexChat) : ChatProvider(plugin, "vanilla"), Listener {

    override fun enable(config: ConfigurationSection?) { }

    override fun reload(config: ConfigurationSection?) {
        // TODO: load formats
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        registerChatter(PlayerChatter(this, e.player))
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        unregisterChatter(e.player.uniqueId.toString())
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onAsyncPlayerChat_Lowest(e: AsyncPlayerChatEvent) {
        if (e.isCancelled) {
            // Return if canceled
            return
        }

        // Set format
        e.format = try {
            Bukkit.getScheduler().callSyncMethod(plugin) {
                val chatter = getChatter(e.player.uniqueId.toString())
                if (chatter == null) {
                    // Something went wrong
                    Message.get(FlexChat::class, "error.unable_to_chat").sendTo(e.player)
                    throw RuntimeException()
                }

                val instance = chatter.activeInstance
                if (instance == null) {
                    // No active instance
                    Message.get(FlexChat::class, "error.channel.active_not_set").sendTo(e.player)
                    throw RuntimeException()
                }

                // TODO: Get permission group, get format, etc.

                return@callSyncMethod null
            }.get()
        } catch (ex: ExecutionException) {
            e.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChat_Highest(e: AsyncPlayerChatEvent) {
        if (e.isCancelled) {
            // Return if canceled
            return
        }

        e.isCancelled = true
        Bukkit.getScheduler().runTask(plugin) {
            try {
                val chatter = getChatter(e.player.uniqueId.toString())
                if (chatter == null) {
                    // Something went wrong
                    Message.get(FlexChat::class, "error.unable_to_chat").sendTo(e.player)
                    return@runTask
                }

                val instance = chatter.activeInstance
                if (instance == null) {
                    // No active instance
                    Message.get(FlexChat::class, "error.channel.active_not_set").sendTo(e.player)
                    return@runTask
                }

                // Send message
                FlexChatAPI.chat.sendMessage(chatter, instance, e.format, e.message)
            } catch (ex: Exception) {
                Message.get(FlexChat::class, "error.unable_to_chat").sendTo(e.player)
                ex.printStackTrace()
            }
        }
    }

}
