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
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.command.argument.ArgumentConfig
import me.st28.flexseries.flexlib.command.argument.ArgumentParseException
import me.st28.flexseries.flexlib.command.argument.ArgumentParser

object ChannelParser : ArgumentParser<Channel>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): Channel? {
        val channelName = raw[0]
        return if (channelName.isEmpty()) {
            FlexChatAPI.chatters.getChatter(context.sender).activeChannel
        } else {
            FlexChatAPI.channels.getChannel(raw[0])
        } ?: throw ArgumentParseException(FlexChat::class, "error.channel.not_found", raw[0])
    }

}

object ChannelInstanceParser : ArgumentParser<ChannelInstance>(2) {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): ChannelInstance? {
        val channel = ChannelParser.parse(context, config, raw)!!
        val instanceName = raw[1]

        return if (instanceName.isNullOrEmpty()) {
            channel.getDefaultInstance()
                    ?: throw ArgumentParseException(FlexChat::class, "error.channel.instance.no_default", channel.name)
        } else {
            channel.getInstance(instanceName)
                    ?: throw ArgumentParseException(FlexChat::class, "error.channel.instance.not_found", channel.color, channel.name, instanceName)
        }
    }

}
