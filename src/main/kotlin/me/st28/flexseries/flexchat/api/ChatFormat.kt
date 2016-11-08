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
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexchat.permission.PermissionNodes
import me.st28.flexseries.flexlib.util.ChatColorUtils
import net.milkbowl.vault.chat.Chat
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ChatFormat {

    companion object {

        internal val variables: MutableMap<String, (Chatter, Channel?) -> String> = HashMap()

        init {
            /* Register default chat variables */

            // Basic player information
            registerVariable("NAME") { chatter, channel -> chatter.name }
            registerVariable("DISPNAME") { chatter, channel -> chatter.displayName }
            registerVariable("WORLD") { chatter, channel ->
                if (chatter is PlayerChatter) chatter.player!!.world.name else ""
            }

            // Channel related
            registerVariable("CHCOLOR") { chatter, channel -> channel?.color.toString() }
            registerVariable("CHTAG") { chatter, channel -> channel?.tag ?: "" }

            // Permission related
            registerVariable("GROUP", fun(chatter: Chatter, channel: Channel?): String {
                if (chatter !is PlayerChatter) {
                    return ""
                }
                return getVaultChat().getPrimaryGroup(null, chatter.player)
            })

            registerVariable("PREFIX", fun(chatter: Chatter, channel: Channel?): String {
                if (chatter !is PlayerChatter) {
                    return ""
                }
                return getVaultChat().getPlayerPrefix(null, chatter.player)
            })

            registerVariable("SUFFIX", fun(chatter: Chatter, channel: Channel?): String {
                if (chatter !is PlayerChatter) {
                    return ""
                }
                return getVaultChat().getPlayerSuffix(null, chatter.player)
            })
        }

        private fun getVaultChat(): Chat {
            return JavaPlugin.getPlugin(FlexChat::class.java).vaultChat!!
        }

        /**
         * Registers a chat variable.
         *
         * @return True if the variable was successfully registered.
         *         False if another variable with the same key was already registered.
         */
        fun registerVariable(key: String, replacer: (Chatter, Channel?) -> String): Boolean {
            if (variables.containsKey(key)) {
                return false
            }

            variables.put(key, replacer)
            return true
        }

        fun applyApplicableColors(chatter: Chatter, message: String): String {
            var ret: String = message

            if (chatter.hasPermission(PermissionNodes.COLOR)) {
                ret = ChatColorUtils.applyColors(ret)
            }

            if (chatter.hasPermission(PermissionNodes.FORMAT)) {
                ret = ChatColorUtils.applyFormats(ret)
            }

            if (chatter.hasPermission(PermissionNodes.MAGIC)) {
                ret = ChatColorUtils.applySingle(ret, ChatColor.MAGIC)
            }

            return ret
        }

    }

    val format: String
    val shouldInheritGroup: Boolean

    constructor(format: String, shouldInheritGroup: Boolean) {
        this.format = format
        this.shouldInheritGroup = shouldInheritGroup
    }

    constructor(config: ConfigurationSection, key: String) {
        if (config.get(key) is String) {
            format = config.get(key) as String
            shouldInheritGroup = true
        } else {
            format = config.getString("format", "(null)")
            shouldInheritGroup = config.getBoolean("inherit", true)
        }
    }

    fun getFormattedResult(chatter: Chatter, channel: Channel): String {
        var returnFormat: String = format

        for ((key, variable) in variables.entries) {
            if (!returnFormat.contains("{$key}")) {
                continue
            }

            returnFormat = returnFormat.replace("{$key}", variable.invoke(chatter, channel))
        }

        return ChatColor.translateAlternateColorCodes('&', returnFormat)
    }

}