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

import me.st28.flexseries.flexchat.api.chatter.Chatter
import org.bukkit.ChatColor
import java.util.*

/**
 * Holds information about a chat channel.
 *
 * @param name The name of the channel.
 * @param description The description of the channel.
 * @param tag The short name of the channel.
 * @param color The default channel color.
 * @param radius The chat radius for the channel.
 *               -1 makes the channel global.
 */
abstract class Channel(
        val name: String,
        val description: String,
        val tag: String,
        val color: ChatColor,
        val radius: Int
) {

    companion object {

        val DEFAULT_INSTANCE = "(default)"

    }

    /**
     * The instances of this channel.
     */
    internal val instances: MutableMap<String, ChannelInstance> = HashMap()

    /**
     * @return A collection of all instances of this channel.
     */
    fun getInstances(): Collection<ChannelInstance> {
        return instances.values
    }

    /**
     * @return A collection of the instances a particular [Chatter] has access to normally.
     */
    open fun getVisibleInstances(chatter: Chatter): Collection<ChannelInstance> {
        return instances.values
    }

    /**
     * Retrieves the default instance of this channel.
     * Essentially a convenience method for `getInstance("")`
     *
     * @return The default [ChannelInstance].
     *         Null if this channel does not have a default instance.
     */
    fun getDefaultInstance(): ChannelInstance? {
        return instances[DEFAULT_INSTANCE]
    }

    /**
     * Retrieves an instance of this channel.
     *
     * @param name The name of the instance. Case insensitive.
     * @return The found [ChannelInstance].
     *         Null if no matching instance was found.
     */
    fun getInstance(name: String): ChannelInstance? {
        return instances[name.toLowerCase()]
    }

}
