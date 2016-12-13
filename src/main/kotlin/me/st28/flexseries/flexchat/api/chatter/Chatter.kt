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

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.PermissionNodes
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.backend.ChannelModule
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.PermissionNode
import me.st28.flexseries.flexlib.permission.withVariables
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.GenericDataContainer
import org.bukkit.command.CommandSender
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
        val channelModule = FlexPlugin.getPluginModule(FlexChat::class, ChannelModule::class)

        val channelSec = config.getConfigurationSection("channels")
        if (channelSec != null) {
            for (channelName in channelSec.getKeys(false)) {
                val channel = channelModule.getChannel(channelName) ?: continue

                channelSec.getStringList(channelName).forEach {
                    channel.getInstance(it)?.addChatter(this, true)
                }
            }
        }

        val rawActiveChannel = config.getString("active.channel")
        val rawActiveInstance = config.getString("active.instance")
        if (rawActiveChannel != null && rawActiveInstance != null) {
            val foundInst = channelModule.getChannel(rawActiveChannel)?.getInstance(rawActiveInstance)

            if (foundInst != null && foundInst.containsChatter(this)) {
                activeInstance = foundInst
            }
        }
    }

    /**
     * Saves this chatter's configuration to the given configuration section.
     */
    open fun save(config: ConfigurationSection) {
        // Clear channel section
        config.set("channels", null)

        // Save channels and instances
        for ((channel, instList) in channels) {
            val key = "channels.${channel.name}"

            if (instList.isEmpty()) {
                config.set(key, null)
                continue
            }

            config.set(key, instList.map { it.name })
        }

        // Save active channel/instance
        if (activeInstance == null) {
            config.set("active", null)
        } else {
            config.set("active.channel", activeChannel!!.name)
            config.set("active.instance", activeInstance!!.name)
        }
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
     * @return True if the instance is normally visible to this chatter.
     */
    fun isInstanceVisible(instance: ChannelInstance): Boolean {
        return instance.channel.getVisibleInstances(this).contains(instance)
    }

    internal fun shouldSendSpecificInstanceMessage(instance: ChannelInstance): Boolean {
        return instance != instance.channel.getDefaultInstance() &&
                (instance.channel.getVisibleInstances(this).size != 1 ||
                channels[instance.channel]!!.size > 1)
    }

    private fun performInstanceJoinCheck(instance: ChannelInstance, silent: Boolean): Boolean {
        if (isInInstance(instance)) {
            if (!silent) {
                if (shouldSendSpecificInstanceMessage(instance)) {
                    Message.get(FlexChat::class, "error.channel.instance.already_joined_specific",
                            instance.channel.color, instance.channel.name, instance.name).sendTo(this)
                } else {
                    Message.get(FlexChat::class, "error.channel.instance.already_joined",
                            instance.channel.color, instance.channel.name).sendTo(this)
                }
            }
            return false
        }
        return true
    }

    private fun performInstanceLeaveCheck(instance: ChannelInstance, silent: Boolean): Boolean {
        if (!isInInstance(instance)) {
            if (!silent) {
                if (shouldSendSpecificInstanceMessage(instance)) {
                    Message.get(FlexChat::class, "error.channel.instance.not_joined_specific",
                            instance.channel.color, instance.channel.name, instance.name).sendTo(this)
                } else {
                    Message.get(FlexChat::class, "error.channel.instance.not_joined",
                            instance.channel.color, instance.channel.name).sendTo(this)
                }
            }
            return false
        }
        return true
    }

    /**
     * Attempts to add this chatter to a [ChannelInstance].
     * This method will perform a permission check.
     *
     * @param instance The instance to add.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join or
     *               any error messages. Default is false.
     *
     * @return SUCCESS if the instance was successfully joined.
     *         ALREADY_JOINED if the instance is already joined.
     *         NO_PERMISSION if the chatter does not have permission to join the channel.
     */
    fun addInstance(instance: ChannelInstance, silent: Boolean = false): ChannelInstance.JoinResult {
        /* Check if instance is already joined */
        if (!performInstanceJoinCheck(instance, silent)) {
            return ChannelInstance.JoinResult.ALREADY_JOINED
        }

        /*
         * Visibility check
         * - Is instance normally visible to chatter?
         * - If not, does chatter have permission to bypass visibility check?
         */
        if (!isInstanceVisible(instance) && !hasPermission(PermissionNodes.BYPASS_VISIBLE)) {
            if (!silent) {
                Message.get(FlexChat::class, "error.instance.not_found",
                        instance.channel.color, instance.channel.name, instance).sendTo(this)
            }
            return ChannelInstance.JoinResult.NOT_VISIBLE
        }

        /*
         * Join permission check
         * - Does chatter have permission to join the channel?
         * - If not, does chatter have permission to bypass the join check?
         */
        if (!hasPermission(PermissionNodes.JOIN.withVariables(instance.channel.name))
                && !hasPermission(PermissionNodes.BYPASS_JOIN))
        {
            if (!silent) {
                Message.get(FlexChat::class, "error.channel.no_permission_join",
                        instance.channel.name).sendTo(this)
            }
            return ChannelInstance.JoinResult.NO_PERMISSION
        }

        return if (addInstanceUnsafe(instance, silent)) {
            ChannelInstance.JoinResult.SUCCESS
        } else {
            // Should never happen since this is handled above
            ChannelInstance.JoinResult.ALREADY_JOINED
        }
    }

    /**
     * Attempts to add this chatter to a [ChannelInstance].
     * This method will not perform any checks.
     *
     * @param instance The instance to add.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join or
     *               any error messages. Default is false.
     *
     * @return True if the instance was successfully added.
     *         False if the chatter is already in the instance.
     */
    fun addInstanceUnsafe(instance: ChannelInstance, silent: Boolean = false): Boolean {
        /* Check if instance is already joined */
        if (!performInstanceJoinCheck(instance, silent)) {
            return false
        }

        instance.chatters.add(this)
        channels.getOrPut(instance.channel, { HashSet() }).add(instance)

        if (!silent) {
            val replacements = arrayOf(displayName, instance.channel.color, instance.channel.name, instance.name)
            val chatters = instance.getVisibleChatters(this)

            // Send vague messages
            Message.get(FlexChat::class, "alert.channel.chatter_joined", *replacements).sendTo(
                    chatters.filter { !it.shouldSendSpecificInstanceMessage(instance) })

            // Send specific messages
            Message.get(FlexChat::class, "alert.channel.chatter_joined_specific", *replacements).sendTo(
                    chatters.filter { it.shouldSendSpecificInstanceMessage(instance) })
        }
        return true
    }

    /**
     * Attempts to remove this chatter from a [ChannelInstance].
     * This method will perform a permission check.
     *
     * @param instance The instance to remove.
     * @param silent If true, will not alert the chatter or chatters in the instance of the leave or
     *               any error messages. Default is false.
     */
    fun removeInstance(instance: ChannelInstance, silent: Boolean = false): ChannelInstance.LeaveResult {
        /* Check if instance is joined */
        if (!performInstanceLeaveCheck(instance, silent)) {
            return ChannelInstance.LeaveResult.NOT_JOINED
        }

        /*
         * Leave permission check
         * - Does chatter have permission to leave the channel?
         * - If not, does chatter have permission to bypass the leave check?
         */
        if (!hasPermission(PermissionNodes.LEAVE.withVariables(instance.channel.name))
                && !hasPermission(PermissionNodes.BYPASS_LEAVE))
        {
            if (!silent) {
                Message.get(FlexChat::class, "error.channel.no_permission_join",
                        instance.channel.name).sendTo(this)
            }
            return ChannelInstance.LeaveResult.NO_PERMISSION
        }

        return if (removeInstanceUnsafe(instance, silent)) {
            return ChannelInstance.LeaveResult.SUCCESS
        } else {
            // Should never happen since this is handled above
            ChannelInstance.LeaveResult.NOT_JOINED
        }
    }

    /**
     * Removes this chatter from a [ChannelInstance].
     * This method will not perform any checks.
     *
     * @param instance The instance to remove.
     * @param silent If true, will not alert the chatter or chatters in the instance of the leave or
     *               any error messages. Default is false.
     *
     * @return True if the instance was successfully removed.
     *         False if the instance is not joined.
     */
    fun removeInstanceUnsafe(instance: ChannelInstance, silent: Boolean = false): Boolean {
        /* Check if instance is joined */
        if (!performInstanceLeaveCheck(instance, silent)) {
            return false
        }

        if (!silent) {
            val replacements = arrayOf(displayName, instance.channel.color, instance.channel.name, instance.name)
            val chatters = instance.getVisibleChatters(this)

            // Send vague messages
            Message.get(FlexChat::class, "alert.channel.chatter_left", *replacements).sendTo(
                    chatters.filter { !it.shouldSendSpecificInstanceMessage(instance) })

            // Send specific messages
            Message.get(FlexChat::class, "alert.channel.chatter_left_specific", *replacements).sendTo(
                    chatters.filter { it.shouldSendSpecificInstanceMessage(instance) })
        }

        // Remove after leave message is sent so this chatter also receives the message
        instance.chatters.remove(this)

        // Unset active instance
        // TODO: Go to next active instance
        if (activeInstance == instance) {
            activeInstance = null
        }

        // Remove instance
        val instances = channels[instance.channel]
        if (instances != null) {
            instances.remove(instance)
            if (instances.isEmpty()) {
                channels.remove(instance.channel)
            }
        }
        return true
    }

    private fun performActiveInstanceCheck(instance: ChannelInstance, silent: Boolean): Boolean {
        if (activeInstance == instance) {
            if (!silent) {
                // Send already active message
                if (shouldSendSpecificInstanceMessage(instance)) {
                    Message.get(FlexChat::class, "error.channel.instance.already_active_specific",
                            instance.channel.color, instance.channel.name, instance.name)
                } else {
                    Message.get(FlexChat::class, "error.channel.instance.already_active",
                            instance.channel.color, instance.channel.name)
                }.sendTo(this)
            }
            return false
        }
        return true
    }

    /**
     * Attempts to set the active channel instance for this chatter.
     * This method will perform permission checks.
     *
     * @param instance The instance to add.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join or
     *               any error messages. Default is false.
     *
     * @return SUCCESS if the instance was successfully joined and set to the active instance.
     *         ALREADY_JOINED if the instance is already the active instance.
     *         NO_PERMISSION if the chatter is not in the instance and doesn't have permission to join.
     */
    fun setActiveInstance(instance: ChannelInstance, silent: Boolean = false): ChannelInstance.JoinResult {
        if (!isInInstance(instance)) {
            val ret = addInstance(instance, silent)
            if (!ret.isSuccess) {
                return ret
            }
        }

        return if (setActiveInstanceUnsafe(instance, silent)) {
            ChannelInstance.JoinResult.SUCCESS
        } else {
            ChannelInstance.JoinResult.ALREADY_JOINED
        }
    }

    /**
     * Sets the active channel instance for this chatter. If the instance is not already joined, it
     * will be added.
     * This method does not perform any checks.
     *
     * @param instance The instance to set as the active instance.
     * @param silent If true, will not alert the chatter or chatters in the instance of the join or
     *               any error messages. Default is false.
     *
     * @return True if the instance was successfully set.
     *         False if the instance is already the active instance.
     */
    fun setActiveInstanceUnsafe(instance: ChannelInstance, silent: Boolean = false): Boolean {
        if (!isInInstance(instance)) {
            addInstanceUnsafe(instance, silent)
        }

        // Check if instance is already active
        if (!performActiveInstanceCheck(instance, silent)) {
            return false
        }

        activeInstance = instance
        if (!silent) {
            // Send channel now active message
            if (shouldSendSpecificInstanceMessage(instance)) {
                Message.get(FlexChat::class, "notice.channel.active_set_specific",
                        instance.channel.color, instance.channel.name, instance.name)
            } else {
                Message.get(FlexChat::class, "notice.channel.active_set",
                        instance.channel.color, instance.channel.name)
            }.sendTo(this)
        }
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

    /**
     * Sends a message to the chatter.
     */
    abstract fun sendMessage(message: Message)

}

fun <T: Chatter> Message.sendTo(chatter: T) {
    chatter.sendMessage(this)
}

fun <T: Chatter> Message.sendTo(chatters: Collection<T>, vararg replacements: Any?) {
    val remaining: MutableList<T> = ArrayList(chatters)
    val specific: MutableList<CommandSender> = ArrayList()

    val it = remaining.iterator()
    while (it.hasNext()) {
        val cur = it.next()
        if (cur is PlayerChatter) {
            specific.add(cur.player)
            it.remove()
        }
    }

    sendTo(specific, replacements)
    remaining.forEach { it.sendMessage(this) }
}
