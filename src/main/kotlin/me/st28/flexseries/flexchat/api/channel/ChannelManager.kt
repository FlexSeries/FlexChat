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
import me.st28.flexseries.flexchat.api.ChatFormat
import java.io.InputStream

/**
 * Represents the API layer of the FlexChat Channel manager.
 */
interface ChannelManager {

    /**
     * Registers a Channel.
     *
     * @param defaultConfig The default configuration for the channel.
     *                      Null by default.
     * @return True if the channel was successfully registered.
     *         False if another channel with the same name is already registered.
     */
    fun registerChannel(channel: Channel, defaultConfig: InputStream? = null): Boolean

    /**
     * Unregisters a Channel.
     *
     * @return True if the channel was successfully unregistered.
     *         False if the channel isn't registered under the manager.
     */
    fun unregisterChannel(channel: Channel): Boolean

    /**
     * @return The default Channel.
     */
    fun getDefaultChannel(): Channel

    /**
     * @return A channel with the specified name.
     *         Null if no channel was found.
     */
    fun getChannel(name: String): Channel?

    /**
     * @return The default global channel chat format.
     */
    fun getDefaultChannelFormat(): ChatFormat

    /**
     * Returns the appropriate private message format for a specified Chatter.
     */
    fun getMessageFormat(sender: Chatter): String

    /**
     * Formats a private message.
     */
    fun formatMessage(sender: Chatter, message: String): String

}