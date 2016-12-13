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
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.PermissionHelper
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

/**
 * Handles in-game chat.
 *
 * This is the default provider, so it is always loaded.
 */
class VanillaChatProvider(plugin: FlexChat) : ChatProvider(plugin, "vanilla"), Listener {

    private companion object {

        val GLOBAL_FORMAT: Pattern = Pattern.compile("\\[g:(\\S+)\\]")

    }

    private lateinit var vaultPerm: Permission
    private lateinit var vaultChat: Chat

    private val formats: MutableMap<String, String> = HashMap()

    override fun enable(config: ConfigurationSection?) {
        // Setup Vault hook
        vaultPerm = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission::class.java)!!.provider
        vaultChat = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat::class.java)!!.provider

        // Register chat variables
        chatVariables.put("{WORLD}", { chatter, instance ->
            if (chatter !is PlayerChatter) {
                return@put null
            }

            return@put chatter.player.world.name
        })

        chatVariables.put("{GROUP}", { chatter, instance ->
            if (chatter !is PlayerChatter) {
                return@put null
            }

            return@put vaultPerm.getPrimaryGroup(chatter.player)
        })

        chatVariables.put("{PREFIX}", { chatter, instance ->
            if (chatter !is PlayerChatter) {
                return@put null
            }

            return@put vaultChat.getPlayerPrefix(null, chatter.player)
        })

        chatVariables.put("{SUFFIX}", { chatter, instance ->
            if (chatter !is PlayerChatter) {
                return@put null
            }

            return@put vaultChat.getPlayerSuffix(null, chatter.player)
        })
    }

    override fun reload(config: ConfigurationSection?) { }

    override fun getChatFormat(chatter: Chatter, instance: ChannelInstance): String? {
        if (chatter !is PlayerChatter) {
            return getDefaultFormat()
        }

        val group = PermissionHelper.getTopGroup(chatter.player, formats.keys)
        val format = formats[group] ?: formats["default"] ?: return getDefaultFormat()

        val matcher = GLOBAL_FORMAT.matcher(format)
        return if (matcher.matches()) {
            getGlobalFormat(matcher.group(1))
        } else {
            format
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val chatter = PlayerChatter(this, e.player)
        registerChatter(chatter)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val chatter = unregisterChatter(e.player.uniqueId.toString()) ?: return

        // Remove instances
        chatter.channels.values.forEach {
            it.forEach { it.chatters.remove(chatter) }
        }
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

                return@callSyncMethod ""
            }.get()
        } catch (ex: ExecutionException) {
            e.isCancelled = true
            ""
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
                FlexChatAPI.chat.sendMessage(chatter, instance, if (e.format.isNullOrEmpty()) null else e.format, e.message)
            } catch (ex: Exception) {
                Message.get(FlexChat::class, "error.unable_to_chat").sendTo(e.player)
                ex.printStackTrace()
            }
        }
    }

}
