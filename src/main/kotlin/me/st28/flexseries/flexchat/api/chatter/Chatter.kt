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
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.backend.channel.ChannelModule
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.sendMessage
import me.st28.flexseries.flexlib.permission.PermissionNode
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

abstract class Chatter {

    val identifier: String

    abstract val name: String

    open val displayName: String
        get() = name

    var activeInstance: ChannelInstance? = null
        private set
    internal val instances: MutableMap<ChannelInstance, Long> = HashMap()

    constructor(identifier: String) {
        this.identifier = identifier
    }

    fun load(config: ConfigurationSection) {
        val channelModule = FlexPlugin.getPluginModule(FlexChat::class, ChannelModule::class)!!

        /* Load channel instances */
        val instanceSec = config.getConfigurationSection("instances")
        if (instanceSec != null) {
            for (chName in instanceSec.getKeys(false)) {
                val channel = channelModule.getChannel(chName) ?: continue

                val instanceNames = instanceSec.getStringList(chName)
                if (instanceNames.isNotEmpty()) {
                    for (label in instanceNames) {
                        val instance = channel.getInstance(label)
                        if (instance != null) {
                            this.instances.put(instance, System.currentTimeMillis())
                        }
                    }
                }/* else { // Unnecessary since default channel instance has a label now
                    val applicableInstances = channel.getApplicableInstances(this)
                    if (applicableInstances.isNotNullOrEmpty()) {
                        // If this chatter only belongs to a single channel instance, put them in it
                    }
                }*/
            }
        }

        /* Add to instances */
        for (inst in this.instances.keys) {
            if (inst.canChatterJoin(this)) {
                inst.addChatter(this)
            }
        }

        // TODO: Somewhere, a check needs to be done if a chatter belongs in all of these instances (or if they have the bypass)

        /* Set active instance */
        val actChName = config.getString("active.channel") ?: return
        val actInstName = config.getString("active.instance")

        val actChannel = channelModule.getChannel(actChName) ?: return
        activeInstance = actChannel.getInstance(actInstName)
        if (activeInstance == null || !instances.containsKey(activeInstance!!)) {
            activeInstance = null
        }
    }

    fun save(config: ConfigurationSection) {
        val toSave: MutableMap<String, MutableList<String>> = HashMap()

        for (instance in instances.keys) {
            val chName = instance.channel.name.toLowerCase()

            if (!toSave.containsKey(chName)) {
                toSave.put(chName, ArrayList())
            }

            toSave[chName]!!.add(instance.label)
        }

        val instanceSec = config.createSection("instances")
        for ((key, value) in toSave.entries) {
            instanceSec.set(key, value)
        }

        config.set("active.channel", activeInstance?.channel?.name ?: null)
        config.set("active.instance", activeInstance?.label ?: null)
    }

    fun getInstanceCount(channel: Channel): Int {
        var count = 0
        for (instance in instances.keys) {
            if (instance.channel == channel) {
                ++count
            }
        }
        return count
    }

    fun isInInstance(instance: ChannelInstance): Boolean = instances.containsKey(instance)

    fun addInstance(instance: ChannelInstance): Boolean {
        val result = instances.put(instance, System.currentTimeMillis()) == null

        if (!instance.containsChatter(this)) {
            instance.addChatter(this)
        }

        return result
    }

    fun removeInstance(instance: ChannelInstance): Boolean {
        val result = instances.remove(instance) != null

        if (instance.containsChatter(this)) {
            instance.removeChatter(this)
        }

        if (activeInstance == instance) {
            activeInstance = null

            if (instances.isNotEmpty()) {
                activeInstance = instances.entries.first().key
            }
        }

        return result
    }

    fun setActiveInstance(instance: ChannelInstance?): Boolean {
        if (instance == null) {
            if (this.activeInstance == null) {
                return false
            } else {
                this.activeInstance == null
                return true
            }
        }

        if (!instances.containsKey(instance)
                || this.activeInstance == instance
                || !instance.channel.getAccessibleInstances(this).contains(instance))
        {
            return false
        }

        this.activeInstance = instance
        instances.put(instance, System.currentTimeMillis())
        return true
    }

    abstract fun hasPermission(permission: String): Boolean

    fun hasPermission(permission: PermissionNode): Boolean = hasPermission(permission.node)

    abstract fun sendMessage(message: String)

    abstract fun sendMessage(message: Message)

}

class ConsoleChatter : Chatter {

    companion object {

        val NAME = "CONSOLE"

    }

    override val name: String
        get() = NAME

    constructor() : super(NAME)

    override fun hasPermission(permission: String): Boolean = true

    override fun sendMessage(message: String) {
        Bukkit.getConsoleSender().sendMessage(message)
    }

    override fun sendMessage(message: Message) {
        Bukkit.getConsoleSender().sendMessage(message)
    }

}

class PlayerChatter : Chatter {

    val uuid: UUID

    val player: Player?
        get() = Bukkit.getPlayer(uuid)

    override val name: String
        get() = player!!.name

    override val displayName: String
        get() = player!!.displayName ?: name

    constructor(uuid: UUID) : super(uuid.toString()) {
        this.uuid = uuid
    }

    override fun hasPermission(permission: String): Boolean {
        return player!!.hasPermission(permission)
    }

    override fun sendMessage(message: String) {
        player?.sendMessage(message)
    }

    override fun sendMessage(message: Message) {
        player?.sendMessage(message)
    }

}

fun Player.getChatter(): PlayerChatter? {
    return FlexChatAPI.chatterManager.getChatter(this) as PlayerChatter?
}

fun CommandSender.getChatter(): Chatter? {
    return FlexChatAPI.chatterManager.getChatter(this)
}
