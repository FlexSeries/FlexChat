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
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import java.util.*

/**
 * Represents an instance of a [Channel].
 *
 * @param name The name of the instance.
 */
class ChannelInstance(val channel: Channel, val name: String) {

    /**
     * The chatters in this channel instance.
     */
    internal val chatters: MutableSet<Chatter> = HashSet()

    /**
     * @return A collection of chatters visible to the given chatter.
     */
    fun getVisibleChatters(chatter: Chatter): Collection<Chatter> {
        return if (channel.radius == -1 || chatter !is PlayerChatter) {
            ArrayList(chatters)
        } else {
            val rad2 = channel.radius * channel.radius
            chatters.filter {
                (it !is PlayerChatter) || (it.player.location.distanceSquared(chatter.player.location) < rad2)
            }
        }
    }

    /**
     * @return True if this instance contains the specified [Chatter].
     */
    fun containsChatter(chatter: Chatter): Boolean {
        return chatters.contains(chatter)
    }

    fun addChatter(chatter: Chatter, silent: Boolean): JoinResult {
        return chatter.addInstance(this, silent)
    }

    fun addChatterUnsafe(chatter: Chatter, silent: Boolean): Boolean {
        return chatter.addInstanceUnsafe(this, silent)
    }

    fun removeChatter(chatter: Chatter, silent: Boolean): LeaveResult {
        return chatter.removeInstance(this, silent)
    }

    fun removeChatterUnsafe(chatter: Chatter, silent: Boolean): Boolean {
        return chatter.removeInstanceUnsafe(this, silent)
    }

    enum class JoinResult {

        /**
         * The chatter successfully joined the channel instance.
         */
        SUCCESS,

        /**
         * The chatter does not have permission to join instances of the channel.
         */
        NO_PERMISSION,

        /**
         * The channel instance is not normally visible to the chatter.
         */
        NOT_VISIBLE,

        /**
         * The chatter is already in the channel instance.
         */
        ALREADY_JOINED;

        val isSuccess: Boolean
            get() = this == SUCCESS

    }

    enum class LeaveResult {

        /**
         * The chatter successfully left the channel instance.
         */
        SUCCESS,

        /**
         * The chatter does not have permission to leave instances of the channel.
         */
        NO_PERMISSION,

        /**
         * The chatter is not in the channel instance.
         */
        NOT_JOINED;

        val isSuccess: Boolean
            get() = this == SUCCESS

    }

}
