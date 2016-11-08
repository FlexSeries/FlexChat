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

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexlib.message.Message
import java.util.*

/**
 * Represents an instance of a channel.
 */
open class ChannelInstance {

    companion object {

        val DEFAULT_LABEL: String = "(default)"

    }

    val channel: Channel
    val label: String

    private val chatters: MutableSet<Chatter> = HashSet()

    constructor(channel: Channel, label: String = DEFAULT_LABEL) {
        this.channel = channel
        this.label = label
    }

    /**
     * @return A separate, easier to identify name to display instead of the label.
     */
    open fun getDisplayName(): String {
        return label
    }

    open fun canChatterJoin(chatter: Chatter): Boolean {
        return true
    }

    /**
     * @return A collection of all chatters in the instance that can receive a message sent by the
     *         provided chatter.
     */
    fun getApplicableChatters(chatter: Chatter): Collection<Chatter> {
        val range = channel.radius
        if (range == 0 || chatter !is PlayerChatter) {
            return chatters
        }

        val returnSet: MutableSet<Chatter> = HashSet()

        val senderLoc = chatter.player!!.location
        val radius = Math.pow(channel.radius.toDouble(), 2.0)

        for (oChatter in chatters) {
            if (oChatter is PlayerChatter) {
                try {
                    val oPlayer = oChatter.player
                    if (oPlayer != null && oPlayer.location.distanceSquared(senderLoc) > radius) {
                        continue
                    }
                } catch (ex: IllegalArgumentException) {
                    // Different worlds
                    continue
                }
            }

            returnSet.add(oChatter)
        }

        return returnSet
    }

    fun containsChatter(chatter: Chatter): Boolean = chatters.contains(chatter)

    fun addChatter(chatter: Chatter): Boolean {
        val result = chatters.add(chatter)

        if (!chatter.isInInstance(this)) {
            chatter.addInstance(this)
        }

        return result
    }

    fun removeChatter(chatter: Chatter): Boolean {
        val result = chatters.remove(chatter)

        if (chatter.isInInstance(this)) {
            chatter.removeInstance(this)
        }

        return result
    }

    /**
     * Removes a chatter from this instance, BUT NOT THE CHANNEL FROM THE CHATTER.
     * This should ONLY be used when the chatter is being unloaded.
     */
    internal fun removeOfflineChatter(chatter: Chatter) = chatters.remove(chatter)

    fun removeAllChatters() {
        HashSet(chatters).forEach { removeChatter(it) }
    }

    fun sendMessage(message: Message) {
        for (chatter in chatters) {
            chatter.sendMessage(message)
        }
    }

    fun alertJoin(chatter: Chatter) {
        val single: MutableList<Chatter> = ArrayList()
        val multiple: MutableList<Chatter> = ArrayList()

        for (oChatter in chatters) {
            if (oChatter.getInstanceCount(channel) > 1) {
                multiple.add(oChatter)
            } else {
                single.add(oChatter)
            }
        }

        val message = Message.get(FlexChat::class, "alerts.channel.chatter_joined",
                chatter.displayName, channel.name, channel.color)

        // Send to chatters that only are in this instance for the channel
        for (oChatter in single) {
            oChatter.sendMessage(message)
        }

        // TODO: Handle chatters in multiple instances of this channel
    }

    fun alertLeave(chatter: Chatter) {
        val single: MutableList<Chatter> = ArrayList()
        val multiple: MutableList<Chatter> = ArrayList()

        for (oChatter in chatters) {
            if (oChatter.getInstanceCount(channel) > 1) {
                multiple.add(oChatter)
            } else {
                single.add(oChatter)
            }
        }

        val message = Message.get(FlexChat::class, "alerts.channel.chatter_left",
                chatter.displayName, channel.name, channel.color)

        // Send to chatters that only are in this instance for the channel
        for (oChatter in single) {
            oChatter.sendMessage(message)
        }

        // TODO: Handle chatters in multiple instances of this channel
    }

}
