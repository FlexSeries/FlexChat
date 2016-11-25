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

import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.api.chatter.Chatter

/**
 * The API layer of the chat manager.
 */
interface ChatManager {

    /**
     * Registers a [ChatProvider].
     *
     * @return True if the provider was successfully registered.
     *         False if a provider with the same name is already registered.
     */
    fun registerProvider(provider: ChatProvider): Boolean

    /**
     * @return The default chat format for a specified [ChatProvider].
     */
    fun getDefaultChatFormat(provider: ChatProvider): String

    /**
     * Processes a format and replaces any variables.
     */
    fun processFormat(chatter: Chatter, instance: ChannelInstance, format: String): String

    /**
     * Sends a message.
     * This method will perform the following checks:
     * - Does the chatter have permission to speak in the channel?
     * - Is the channel muted?
     *
     * @param chatter The [Chatter] sending the message.
     * @param instance The [ChannelInstance] to send the message to.
     * @param message The message to send.
     */
    fun sendMessage(chatter: Chatter, instance: ChannelInstance, format: String?, message: String)

    /**
     * Like [sendMessage], but does not perform any checks.
     */
    fun sendMessageUnsafe(chatter: Chatter, instance: ChannelInstance, format: String?, message: String)

}
