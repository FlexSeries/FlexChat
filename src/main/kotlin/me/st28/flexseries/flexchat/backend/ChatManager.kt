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
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.format.ChatFormat
import me.st28.flexseries.flexchat.permission.PermissionNodes
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.PermissionNode
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*

class ChatManager : FlexModule<FlexChat>, Listener {

    constructor(plugin: FlexChat) : super(plugin, "chat", "Manages chat")

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onAsyncPlayerChat_lowest(e: AsyncPlayerChatEvent) {
        Bukkit.getScheduler().callSyncMethod(plugin, {
            val player = e.player

            val chatter = FlexChatAPI.chatterManager.getChatter(player)
            if (chatter == null) {
                println("Chatter is null")

                e.isCancelled = true
                Message.get(FlexChat::class, "error.unable_to_chat").sendTo(player)
                LogHelper.severe(this, "Player '${player.name}' failed to chat: No chatter instance found")
                return@callSyncMethod
            }

            val active = chatter.activeInstance
            if (active == null) {
                println("Active instance is null")

                e.isCancelled = true
                Message.get(FlexChat::class, "error.channel.active_not_set").sendTo(player)
                return@callSyncMethod
            }

            if (!chatter.hasPermission(PermissionNode.buildVariableName(PermissionNodes.CHAT, active.channel.name))) {
                println("Chatter does not have permission to chat in channel")

                e.isCancelled = true
                Message.get(FlexChat::class, "error.channel.no_permission_chat").sendTo(player)
                return@callSyncMethod
            }

            e.format = active.channel.getChatFormat(chatter).getFormattedResult(chatter, active.channel)
        }).get()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChat_highest(e: AsyncPlayerChatEvent) {
        e.isCancelled = true // FlexChat will handle sending the message

        SchedulerUtils.runSync(plugin, {
            val player = e.player

            // Get the chatter. If null, send an error message.
            val chatter = FlexChatAPI.chatterManager.getChatter(player)
            if (chatter == null) {
                Message.get(FlexChat::class, "error.unable_to_chat").sendTo(player)
                LogHelper.severe(this, "Player '${player.name}' failed to chat: No chatter instance found")
                return@runSync
            }

            // Get the active instance. If null, send an error message.
            val active = chatter.activeInstance
            if (active == null) {
                e.isCancelled = true
                Message.get(FlexChat::class, "error.channel.active_not_set").sendTo(player)
                return@runSync
            }

            // Check if the channel is muted
            if (active.channel.isMuted && !chatter.hasPermission(PermissionNodes.BYPASS_MUTE)) {
                Message.get(FlexChat::class, "error.unable_to_chat_channel_muted", active.channel.name).sendTo(player)
                return@runSync
            }

            // Determine who the receipients who
            val recipients: MutableList<Chatter> = ArrayList(active.getApplicableChatters(chatter))

            // TODO: Ignored users
            /*if (!chatter.hasPermission(PermissionNodes.BYPASS_IGNORE)) {
                // Only look for ignore exclusions if the chatter cannot bypass ignore
            }*/

            // Send the message, call new event, and log to the console
            val sendMessage = e.format.replace("{MESSAGE}", ChatFormat.applyApplicableColors(chatter, e.message))

            recipients.forEach { it.sendMessage(sendMessage) }

            //ChatLogHelper.log(active, ChatColor.stripColor(e.format.replace("{MESSAGE}", e.message)))
        })
    }

}