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
package me.st28.flexseries.flexchat.api.chatter

import me.st28.flexseries.flexchat.PermissionNodes
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexlib.permission.PermissionNode
import me.st28.flexseries.flexlib.util.GenericDataContainer
import org.bukkit.configuration.ConfigurationSection
import java.util.*

/**
 * Represents an entity that can chat and receive messages.
 *
 * @param provider The chat provider that this chatter is from.
 * @param identifier The identifier of this chatter.
 */
abstract class Chatter(val provider: ChatProvider, val identifier: String) {

    /**
     * The name of the chatter.
     */
    open val name: String
        get() = identifier

    /**
     * The display name of the chatter.
     */
    open val displayName: String
        get() = identifier

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
     * The chatter's active channel.
     */
    val activeChannel: Channel?
        get() = activeInstance?.channel

    /**
     * Loads this chatter's configuration from the given configuration section.
     */
    open fun load(config: ConfigurationSection) {
        // TODO: Load channels + instances
    }

    /**
     * Saves this chatter's configuration to the given configuration section.
     */
    open fun save(config: ConfigurationSection) {
        // TODO: Save channels + instances
    }

    /**
     * @return A collection of [Channel]s this player is in one or more instances of.
     */
    fun getChannels(): Collection<Channel> {
        return channels.keys
    }

    /**
     * @return True if the chatter is in at least one instance of the specified [Channel].
     */
    fun isInChannel(channel: Channel): Boolean {
        return channels.containsKey(channel)
    }

    /**
     * @return True if the chatter is in the specified [ChannelInstance].
     */
    fun isInInstance(instance: ChannelInstance): Boolean {
        return channels[instance.channel]?.contains(instance) ?: false
    }

    /**
     * Attempts to add this chatter to a [ChannelInstance].
     * This method will perform a permission check.
     *
     * @param instance The instance to add.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join.
     *               Default is false.
     * @return SUCCESS if the instance was successfully joined.
     *         ALREADY_JOINED if the instance is already joined.
     *         NO_PERMISSION if the chatter does not have permission to join the channel.
     *
     */
    fun addInstance(instance: ChannelInstance, silent: Boolean = false): ChannelInstance.JoinResult {
        // Check if already joined
        if (isInInstance(instance)) {
            return ChannelInstance.JoinResult.ALREADY_JOINED
        }

        // Join permission check
        if (!hasPermission(PermissionNode.buildVariableNode(PermissionNodes.JOIN, instance.channel.name))) {
            if (!silent) {
                // TODO: Send no permission message
            }
            return ChannelInstance.JoinResult.NO_PERMISSION
        }

        return if (addInstanceUnsafe(instance, silent)) {
            ChannelInstance.JoinResult.SUCCESS
            // TODO: Send join message, etc.
        } else {
            // Should never happen since this is handled above
            ChannelInstance.JoinResult.ALREADY_JOINED
        }
    }

    /**
     * Attempts to add this chatter to a [ChatterInstance].
     * This method will not perform any checks.
     *
     * @param instance The instance to add.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join.
     *               Default is false.
     *
     * @return True if the instance was successfully added.
     *         False if the chatter is already in the instance.
     */
    fun addInstanceUnsafe(instance: ChannelInstance, silent: Boolean = false): Boolean {
        if (isInInstance(instance)) {
            return false
        }

        instance.chatters.add(this)
        channels.getOrPut(instance.channel, { HashSet() }).add(instance)

        // TODO: Announce join to instance members

        return true
    }

    /**
     * Checks if the chatter has a specified permission.
     */
    abstract fun hasPermission(permission: String): Boolean

    fun hasPermission(permission: PermissionNode): Boolean = hasPermission(permission.node)

    /**
     * Sends a message to the chatter.
     */
    abstract fun sendMessage(message: String)

}
