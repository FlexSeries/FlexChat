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

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.api.channel.ChannelManager
import me.st28.flexseries.flexchat.api.chatter.ChatterManager
import me.st28.flexseries.flexchat.backend.ChannelModule
import me.st28.flexseries.flexchat.backend.ChatModule
import me.st28.flexseries.flexchat.backend.ChatterModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin

/**
 * The API layer for FlexChat.
 */
object FlexChatAPI {

    val channels: ChannelManager
        get() = FlexPlugin.getPluginModule(FlexChat::class, ChannelModule::class)!!

    val chatters: ChatterManager
        get() = FlexPlugin.getPluginModule(FlexChat::class, ChatterModule::class)!!

    val chat: ChatManager
        get() = FlexPlugin.getPluginModule(FlexChat::class, ChatModule::class)!!

}
