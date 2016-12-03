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
package me.st28.flexseries.flexchat.commands

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.backend.ChatModule
import me.st28.flexseries.flexchat.backend.ChatterModule
import me.st28.flexseries.flexlib.command.CommandHandler
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.list.ListBuilder
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.command.CommandSender

object CmdFlexChat {

    @CommandHandler(
            "flexchat stats",
            description = "View plugin usage and stats",
            permission = "flexchat.stats"
    )
    fun stats(sender: CommandSender): ListBuilder {
        val builder = ListBuilder()

        val chatterModule = FlexPlugin.getPluginModule(FlexChat::class, ChatterModule::class)
        val chatModule = FlexPlugin.getPluginModule(FlexChat::class, ChatModule::class)

        builder.header("title", "FlexChat Stats")
        builder.element("element_description", "Chatters", chatterModule.chatters.size.toString())
        builder.element("element_description", "Providers", chatModule.providers.keys.joinToString(", "))

        return builder
    }

    @CommandHandler(
            "flexchat chatter",
            description = "View information about a chatter",
            permission = "flexchat.stats"
    )
    fun chatter(sender: CommandSender, chatter: String): Any {
        val chatterModule = FlexPlugin.getPluginModule(FlexChat::class, ChatterModule::class)

        val foundChatter = chatterModule.getChatterByName(chatter)  // Try by name
                ?: chatterModule.getChatter(chatter)  // Try by identifier
                ?: return Message.get(FlexChat::class, "error.chatter.not_found", chatter)

        val builder = ListBuilder()

        builder.header("subtitle", "Chatter", foundChatter.name)

        builder.element("element_description", "Identifier", foundChatter.identifier)
        builder.element("element_description", "Name", foundChatter.name)
        builder.element("element_description", "Display Name", foundChatter.displayName)
        builder.element("element_description", "Active channel", foundChatter.activeChannel?.name ?: "(none)")
        builder.element("element_description", "Active instance", if (foundChatter.activeInstance != null) {
            if (foundChatter.activeInstance!!.name.isEmpty()) {
                "(default)"
            } else {
                foundChatter.activeInstance!!.name
            }
        } else {
            "(none)"
        })

        builder.element("element_description", "Channels", if (foundChatter.channels.isEmpty()) {
            "(none)"
        } else {
            foundChatter.channels.keys.sortedBy { it.name }.joinToString(", ") { it.name }
        })

        return builder
    }

}
