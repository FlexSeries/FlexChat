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

/**
 * Represents the API layer of the [Channel] manager.
 */
interface ChannelManager {

    /**
     * @return A collection of all loaded [Channel]s.
     */
    fun getChannels(): Collection<Channel>

    /**
     * Retrieves a channel based on its name.
     *
     * @param name The name of the channel. Case insensitive.
     * @return The found [Channel].
     *         Null if no matching channel was found.
     */
    fun getChannel(name: String): Channel?

    /**
     * Retrieves a channel based on its tag.
     *
     * @param tag The tag of the channel. Case insensitive.
     *
     * @return The found [Channel].
     *         Null if no matching channel was found.
     */
    fun getChannelByTag(tag: String): Channel?

    /**
     * @return The default [Channel], as defined in the configuration file.
     *         Null if no default channel is defined, or it doesn't exist.
     */
    fun getDefaultChannel(): Channel?

    /**
     * Registers a [Channel].
     *
     * @return True if the channel was successfully registered.
     *         False if a channel with the same name is already registered.
     */
    fun registerChannel(channel: Channel): Boolean

}
