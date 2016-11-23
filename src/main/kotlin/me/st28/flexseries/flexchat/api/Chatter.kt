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

import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import me.st28.flexseries.flexlib.util.GenericDataContainer
import java.util.*

/**
 * Represents an entity that can chat and receive messages.
 *
 * @param provider The chat provider that this chatter is from.
 * @param identifier The identifier of this chatter.
 * @param file The data file for this chatter.
 */
class Chatter(val provider: ChatProvider, val identifier: String, val file: YamlFileManager) {

    /**
     * Custom data for this chatter.
     */
    val data: GenericDataContainer = GenericDataContainer()

    /**
     * Stores the channels and instances this chatter is in.
     */
    internal val channels: MutableMap<Channel, MutableSet<ChannelInstance>> = HashMap()

    /**
     * The chatter's active channel instance.
     */
    internal var activeInstance: ChannelInstance? = null

    /**
     * Sends a message to the chatter.
     *
     * Convenience method for [ChatProvider.sendMessage]
     */
    fun sendMessage(message: String) {
        provider.sendMessage(this, message)
    }

}
