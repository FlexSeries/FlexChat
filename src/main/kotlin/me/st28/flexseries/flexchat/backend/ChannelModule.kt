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
import me.st28.flexseries.flexchat.api.Channel
import me.st28.flexseries.flexchat.api.ChannelInstance
import me.st28.flexseries.flexchat.api.ChannelManager
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import me.st28.flexseries.flexlib.util.translateColorCodes
import org.apache.commons.lang.StringEscapeUtils
import java.util.*

class ChannelModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "channels", "Manages channels"), ChannelManager {

    private var defaultChannel: String? = null
    private val channels: MutableMap<String, Channel> = HashMap()

    private lateinit var defaultDescription: String

    private lateinit var activeChannelSymbol: String
    private lateinit var activeInstanceSymbol: String

    /**
     * Loaded channels that have been loaded from configuration files.
     */
    private val loadedChannels: MutableSet<String> = HashSet()

    override fun handleReload(isFirstReload: Boolean) {
        if (isFirstReload) {
            return
        }

        val config = getConfig()

        /* Reload configuration */
        defaultDescription = config.getString("default description", "&c&oNo description set")
        activeChannelSymbol = StringEscapeUtils.unescapeJava(config.getString("active symbol.channel")).translateColorCodes()
        activeInstanceSymbol = StringEscapeUtils.unescapeJava(config.getString("active symbol.instance")).translateColorCodes()

        /* Reload channels */
        channels.clear()
        loadedChannels.clear()

        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        for (file in dataFolder.listFiles { f -> f.endsWith(".channel.yml") }) {
            val yaml = YamlFileManager(file).config

            val channel = BasicChannel(
                    yaml.getString("name"),
                    yaml.getString("description", defaultDescription).translateColorCodes()
            )
            channel.instances.put("", ChannelInstance(""))

            channels.put(channel.name.toLowerCase(), channel)
            loadedChannels.add(channel.name.toLowerCase())
        }

        LogHelper.info(this, "Loaded ${loadedChannels.size} channel(s)")

        /* Reload default channel */
        defaultChannel = config.getString("default channel")
        if (defaultChannel.isNullOrEmpty()) {
            defaultChannel = null
            LogHelper.info(this, "No default chat channel is defined")
        } else {
            LogHelper.info(this, "Default chat channel is set to '$defaultChannel'")

            if (!channels.containsKey(defaultChannel!!)) {
                LogHelper.warning(this, "Default chat channel is not loaded")
            }
        }
    }

    override fun registerChannel(channel: Channel): Boolean {
        val key = channel.name.toLowerCase()
        if (channels.containsKey(key)) {
            return false
        }

        channels.put(key, channel)
        LogHelper.info(this, "Registered channel '${channel.name}'")
        return true
    }

    override fun getDefaultChannel(): Channel? {
        return channels[defaultChannel]
    }

    override fun getChannel(name: String): Channel? {
        return channels[name.toLowerCase()]
    }

}
