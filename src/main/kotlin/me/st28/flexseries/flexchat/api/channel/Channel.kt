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
package me.st28.flexseries.flexchat.api.channel

import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexchat.api.format.ChatFormat
import me.st28.flexseries.flexchat.backend.channel.ChannelModule
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.PermissionHelper
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import java.util.*

abstract class Channel {

    var name: String
        private set
    var fileName: String?
        private set
    var tag: String = ""
        private set

    var description: String = ""
        private set
    var color: ChatColor = ChatColor.WHITE
        private set
    var radius: Int = 0
        private set

    internal var isMuted: Boolean = false
    internal var muteTime: Int = -1

    private val formats: MutableMap<String, ChatFormat> = LinkedHashMap()

    constructor(name: String, fileName: String? = null) {
        this.name = name
        this.fileName = fileName
    }

    internal fun reload(channelManager: ChannelModule, config: ConfigurationSection) {
        name = config.getString("name")!!
        tag = config.getString("tag", "")

        description = config.getString("description", "")

        color = ChatColor.valueOf(config.getString("color", "WHITE").toUpperCase())

        radius = config.getInt("chat radius", 0)
        if (radius < 0) {
            radius = 0
        }

        formats.clear()
        val formatSec: ConfigurationSection? = config.getConfigurationSection("formats")
        if (formatSec != null) {
            // TODO: Load per-channel formats
            /*for (group in formatSec.getKeys(false)) {
                val format = ChatFormat()
            }*/
        }

        if (!formats.containsKey("default")) {
            formats.put("default", FlexChatAPI.channelManager.getDefaultChannelFormat())
        }
    }

    fun getChatFormat(group: String?): ChatFormat {
        return formats[if (group == null) "default" else group.toLowerCase()]!!
    }

    fun getChatFormat(chatter: Chatter): ChatFormat {
        if (chatter !is PlayerChatter) {
            return getChatFormat(null)
        }
        return formats[PermissionHelper.getTopGroup(chatter.player!!, ArrayList(formats.keys), "default")]!!
    }

    /**
     * @return A collection of all ChannelInstances of this Channel.
     */
    abstract fun getInstances(): Collection<ChannelInstance>

    /**
     * @return A channel instance by its name.
     *         Null if no instance with the specified name exists.
     */
    fun getInstance(name: String): ChannelInstance? {
        for (inst in getInstances()) {
            if (inst.getDisplayName().equals(name, true) || inst.label.equals(name, true)) {
                return inst
            }
        }
        return null
    }

    /**
     * @return A collection of all ChannelInstance of this Channel that a specified Chatter
     *         has access to.
     */
    abstract fun getAccessibleInstances(chatter: Chatter): Collection<ChannelInstance>

    /**
     * Sends a message to all instances of this channel.
     */
    fun sendMessage(message: Message) = getInstances().forEach { it.sendMessage(message) }

}