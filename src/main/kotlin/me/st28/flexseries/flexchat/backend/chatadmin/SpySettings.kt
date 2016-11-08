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
package me.st28.flexseries.flexchat.backend.chatadmin

import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class SpySettings {

    var isEnabled: Boolean

    // Added channel means that all instances are being spied on
    val channels: MutableSet<String> = HashSet()

    // If only individual instances of a channel are being spied on, they are specified here
    val instances: MutableMap<String, MutableSet<String>> = HashMap()

    internal constructor(config: ConfigurationSection?) {
        if (config == null) {
            isEnabled = false
            return
        }

        isEnabled = config.getBoolean("enabled", false)

        channels.addAll(config.getStringList("channels"))

        val instanceSec = config.getConfigurationSection("instances") ?: return
        for (channel in instanceSec.getKeys(false)) {
            if (!instances.containsKey(channel.toLowerCase())) {
                instances.put(channel.toLowerCase(), HashSet())
            }
            instances[channel.toLowerCase()]!!.addAll(instanceSec.getStringList(channel))
        }
    }

    internal fun save(config: ConfigurationSection) {
        config.set("enabled", isEnabled)
        config.set("channels", ArrayList(channels))

        val instanceSec = config.getConfigurationSection("instances")!!
        for ((key, value) in instances.entries) {
            val list = ArrayList(value)
            if (list.isEmpty()) {
                continue
            }

            instanceSec.set(key, list)
        }

    }

    fun containsChannel(channel: Channel): Boolean {
        return channels.contains(channel.name.toLowerCase())
    }

    fun addChannel(channel: Channel): Boolean {
        return channels.add(channel.name.toLowerCase())
    }

    fun removeChannel(channel: Channel): Boolean {
        return channels.remove(channel.name.toLowerCase())
    }

    fun containsInstance(instance: ChannelInstance): Boolean  {
        if (containsChannel(instance.channel)) {
            return true
        }

        val channel = instance.channel.name.toLowerCase()
        if (!instances.containsKey(channel)) {
            return false
        }

        return instances[channel]!!.contains(instance.label.toLowerCase())
    }

    fun addInstance(instance: ChannelInstance): Boolean {
        return false
    }

    fun removeInstance(instance: ChannelInstance): Boolean {
        return false
    }

}