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
package me.st28.flexseries.flexchat

import me.st28.flexseries.flexchat.backend.ChatManager
import me.st28.flexseries.flexchat.backend.channel.ChannelModule
import me.st28.flexseries.flexchat.backend.chatter.ChatterModule
import me.st28.flexseries.flexchat.commands.ChatterResolver
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import net.milkbowl.vault.chat.Chat

class FlexChat : FlexPlugin() {

    var vaultChat: Chat? = null

    override fun handleLoad() {
        registerModule(ChannelModule(this))
        registerModule(ChatterModule(this))
        registerModule(ChatManager(this))
    }

    override fun handleEnable() {
        val chatProvider = server.servicesManager.getRegistration(net.milkbowl.vault.chat.Chat::class.java)
        vaultChat = chatProvider.provider

        ArgumentResolver.register(this, "chatter", ChatterResolver)
        commandMap.register(me.st28.flexseries.flexchat.commands.Messaging)
    }

}