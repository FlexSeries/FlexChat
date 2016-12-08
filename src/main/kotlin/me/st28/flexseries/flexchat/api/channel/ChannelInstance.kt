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
import me.st28.flexseries.flexchat.PermissionNodes
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexchat.api.chatter.sendTo
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.withVariables
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

    /**
     * @see Chatter.addInstance
     */
    fun addChatter(chatter: Chatter, silent: Boolean): JoinResult {
        return chatter.addInstance(this, silent)
    }

    /**
     * @see Chatter.addInstanceUnsafe
     */
    fun addChatterUnsafe(chatter: Chatter, silent: Boolean): Boolean {
        return chatter.addInstanceUnsafe(this, silent)
    }

    /**
     * @see Chatter.removeInstance
     */
    fun removeChatter(chatter: Chatter, silent: Boolean): LeaveResult {
        return chatter.removeInstance(this, silent)
    }

    /**
     * @see Chatter.removeInstanceUnsafe
     */
    fun removeChatterUnsafe(chatter: Chatter, silent: Boolean): Boolean {
        return chatter.removeInstanceUnsafe(this, silent)
    }

    private fun performChatterKickCheck(chatter: Chatter, kicker: Chatter?, silent: Boolean): Boolean {
        if (!containsChatter(chatter)) {
            if (!silent && kicker != null) {
                val replacements = arrayOf(chatter.displayName, channel.color, channel.name, name)
                if (kicker.shouldSendSpecificInstanceMessage(this)) {
                    Message.get(FlexChat::class, "error.chatter.not_in_instance_specific", *replacements)
                } else {
                    Message.get(FlexChat::class, "error.chatter.not_in_instance", *replacements)
                }
            }
            return false
        }
        return true
    }

    /**
     * Kicks a chatter from the instance.
     * This method will perform permission checks.
     *
     * @param chatter The [Chatter] to kick.
     * @param kicker The [Chatter] performing the kick.
     *               Null to send a generic kick message.
     * @param silent If true, will not send any messages.
     *
     * @return SUCCESS if the chatter was successfully kicked.
     *         NO_PERMISSION if the kicker is not null and doesn't have permission to kick.
     *         NOT_JOINED if the target is not in the instance.
     */
    fun kickChatter(chatter: Chatter, kicker: Chatter? = null, silent: Boolean): LeaveResult {
        // Perform permission check
        if (kicker != null && !kicker.hasPermission(PermissionNodes.KICK.withVariables(channel.name))) {
            if (!silent) {
                Message.get(FlexChat::class, "error.channel.no_permission_kick").sendTo(kicker)
            }
            return LeaveResult.NO_PERMISSION
        }

        // Perform join check
        if (!performChatterKickCheck(chatter, kicker, silent)) {
            return LeaveResult.NOT_JOINED
        }

        if (kickChatterUnsafe(chatter, kicker, silent)) {
            return LeaveResult.SUCCESS
        } else {
            // Should never happen since this is handled above
            return LeaveResult.NOT_JOINED
        }
    }

    /**
     * Kicks a chatter from the instance.
     * This method does not perform any checks.
     *
     * @param chatter The [Chatter] to kick.
     * @param kicker The [Chatter] performing the kick.
     *               Null to send a generic kick message.
     * @param silent If true, will not send any messages.
     *
     * @return True if the chatter was successfully kicked.
     *         False if the chatter is not in this instance.
     */
    fun kickChatterUnsafe(chatter: Chatter, kicker: Chatter? = null, silent: Boolean): Boolean {
        if (!performChatterKickCheck(chatter, kicker, silent)) {
            return false
        }

        if (!silent) {
            val replacements = arrayOf(chatter.displayName, channel.color, channel.name, name, kicker?.name)

            val messageKey = if (kicker != null) {
                "alert.channel.chatter_kicked_by"
            } else {
                "alert.channel.chatter_kicked"
            }

            // Send vague messages
            Message.get(FlexChat::class, messageKey, *replacements).sendTo(
                    chatters.filter { !it.shouldSendSpecificInstanceMessage(this) })

            // Send specific messages
            Message.get(FlexChat::class, "${messageKey}_specific", *replacements).sendTo(
                    chatters.filter { it.shouldSendSpecificInstanceMessage(this) })
        }
        removeChatterUnsafe(chatter, true)
        return true
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
