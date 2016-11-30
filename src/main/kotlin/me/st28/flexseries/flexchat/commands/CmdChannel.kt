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
import me.st28.flexseries.flexchat.PermissionNodes
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.getChatter
import me.st28.flexseries.flexchat.backend.ChannelModule
import me.st28.flexseries.flexlib.command.CommandHandler
import me.st28.flexseries.flexlib.command.argument.Default
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.list.ListBuilder
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

object CmdChannel {

    @CommandHandler(
            "channel",
            description = "Change active channel"
    )
    fun switch(sender: CommandSender, channel: String, instance: String?): Message? {
        val foundChannel = FlexChatAPI.channels.getChannel(channel)
            ?: return Message.get(FlexChat::class, "error.channel.not_found", channel)

        val foundInstance = if (instance != null) {
            foundChannel.getInstance(instance)
                ?: return Message.get(FlexChat::class, "error.instance.not_found", instance, foundChannel.name)
        } else {
            foundChannel.getDefaultInstance()
                ?: return Message.get(FlexChat::class, "error.instance.no_default", foundChannel.name)
        }

        // Set active instance. No messages need to be sent here since setActiveInstance handles it
        FlexChatAPI.chatters.getChatter(sender).setActiveInstance(foundInstance)
        return null
    }

    @CommandHandler(
            "channel join",
            description = "Join a channel"
    )
    fun join(sender: CommandSender, channel: String, instance: String?): Message? {
        return null
    }

    @CommandHandler(
            "channel list",
            description = "List channels",
            permission = "flexchat.channels.list"
    )
    fun list(sender: CommandSender, @Default("1") page: Int): ListBuilder {
        val module = FlexPlugin.getPluginModule(FlexChat::class, ChannelModule::class)!!
        val channels = ArrayList(FlexChatAPI.channels.getChannels())

        val chatter = sender.getChatter()
        val activeChannel = chatter.activeChannel

        /*
         * Channel Sort Order:
         * 1) Active channel
         * 2) Join channels (alphabetical)
         * 3) Unjoined channels (alphabetical)
         */
        channels.sort { c1, c2 ->
            if (c1 == c2) {
                return@sort 0
            }

            if (c2 == activeChannel) {
                // If the other channel is active, it is automatically first.
                return@sort 1
            } else if (c1 == activeChannel) {
                // If the first channel is active, it is automatically first.
                return@sort -1
            }

            val isJoin1 = chatter.isInChannel(c1)
            val isJoin2 = chatter.isInChannel(c2)

            if (isJoin1 != isJoin2) {
                // If only one of the channels is joined, the joined one is first.
                return@sort if (isJoin1) 1 else -1
            } else {
                // If both channels are joined or unjoined, they are sorted alphabetically
                return@sort c1.name.compareTo(c2.name, true)
            }
        }

        val builder = ListBuilder()

        builder.page(page, channels.size)
        builder.header("page", "Chat Channels")

        for (channel in channels) {
            builder.element("flexchat_list_channel",
                    // Active
                    if (channel == activeChannel) {
                        module.activeChannelSymbol
                    } else {
                        ""
                    },

                    // Color
                    channel.color.toString(),

                    // Name
                    channel.name,

                    // Status
                    if (chatter.isInChannel(channel)) {
                        "${ChatColor.GREEN}joined"
                    } else {
                        "${ChatColor.RED}not joined"
                    }
            )
        }

        return builder
    }

}
