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

import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.getChatter
import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.command.CommandHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object Messaging {

    @CommandHandler(
        "message",
        description = "Send a private message to another user",
        aliases = arrayOf("msg", "m", "tell", "t", "whisper", "w"),
        args = arrayOf(
            "player flexchat::chatter always",
            "message string always"
        )
    )
    fun message(sender: CommandSender, context: CommandContext) {
        val senderChatter = sender.getChatter()!!
        val targetChatter = context.getArgument<Chatter>("player")!!

        // TODO: Check for ignore

        val rawMessage: String = context.getArgument("message")!!
        val message = FlexChatAPI.channelManager.formatMessage(senderChatter, rawMessage)

        // Send message
        sender.sendMessage(message.replace("{SENDER}", "${ChatColor.ITALIC}me").replace("{RECEIVER}", targetChatter.displayName))
        targetChatter.sendMessage(message.replace("{SENDER}", senderChatter.displayName).replace("{RECEIVER}", "${ChatColor.ITALIC}me"))

        // TODO: Set reply reference

        // TODO: Log
    }

    fun reply(sender: CommandSender, context: CommandContext) {

    }

}