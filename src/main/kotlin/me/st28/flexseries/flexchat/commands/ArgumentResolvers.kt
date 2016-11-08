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
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.command.argument.ArgumentConfig
import me.st28.flexseries.flexlib.command.argument.ArgumentResolveException
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver
import me.st28.flexseries.flexlib.command.argument.PlayerResolver
import org.bukkit.Bukkit

internal object ChatterResolver : ArgumentResolver<Chatter>(false) {

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): Chatter? {
        if (input.equals("console", true)) {
            return FlexChatAPI.chatterManager.getChatter(Bukkit.getConsoleSender())
        }

        val player = PlayerResolver.resolve(context, config, input)
        if (player != null) {
            return FlexChatAPI.chatterManager.getChatter(player.online!!)
        }
        throw ArgumentResolveException(FlexChat::class, "error.chatter.not_found", input)
    }

    /*override fun resolveAsync(context: CommandContext, config: ArgumentConfig, input: String): Chatter? {
        PlayerResolver.resolveAsync(context, config, input)
        FlexChatAPI.chatterManager.getChatter()
        return FlexChatAPI.chatterManager.getChatter(PlayerResolver.resolveAsync(context, config, input)!!.online!!)
    }*/

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        return null
    }

}

