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
package me.st28.flexseries.flexchat.backend.channel

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.api.channel.Channel
import me.st28.flexseries.flexchat.api.channel.ChannelManager
import me.st28.flexseries.flexchat.api.format.ChatFormat
import me.st28.flexseries.flexchat.backend.chatter.ChatterModule
import me.st28.flexseries.flexchat.permission.PermissionNodes
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.permission.PermissionNode
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import org.apache.commons.lang.StringEscapeUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.InputStream
import java.util.*

class ChannelModule : FlexModule<FlexChat>, ChannelManager {

    companion object {

        val GLOBAL_FORMAT_PATTERN: Regex = Regex("\\[g:(\\S+)\\]")

    }

    var activeChannelSymbol: String = ""
        private set
    var activeInstanceSymbol: String = ""
        private set

    var defaultChannel: String = ""
        private set
    var defaultChannelDescription: String = ""
        private set

    /* Format related */
    private val globalFormats: MutableMap<String, ChatFormat> = LinkedHashMap()
    private val messageFormats: MutableMap<String, String> = HashMap()

    private val loadedChannels: MutableSet<String> = HashSet()
    private val channels: MutableMap<String, Channel> = HashMap()

    private val muteRunnables: MutableMap<String, ChannelUnmuteRunnable> = HashMap()

    constructor(plugin: FlexChat) : super(plugin, "channels", "Manages chat channels")

    override fun handleEnable() {
        /*customChannelDir = File(dataFolder.path + File.separator + "custom")
        customChannelDir.mkdirs()*/
    }

    override fun handleReload(isFirstReload: Boolean) {
        if (isFirstReload) {
            return
        }

        val config = getConfig()

        defaultChannelDescription = ChatColor.translateAlternateColorCodes('&',
                StringEscapeUtils.unescapeJava(
                        config.getString("default description", "&c&oNo description set")
                )
        )

        activeChannelSymbol = ChatColor.translateAlternateColorCodes('&',
                StringEscapeUtils.unescapeJava(config.getString("active symbol.channel", "\u25B6")))
        activeInstanceSymbol = ChatColor.translateAlternateColorCodes('&',
                StringEscapeUtils.unescapeJava(config.getString("active symbol.instance", "\u25B6")))

        /* Load formats */
        globalFormats.clear()
        val globalFormatSec = config.getConfigurationSection("global formats")
        if (globalFormatSec != null) {
            for (group in globalFormatSec.getKeys(false)) {
                globalFormats.put(group.toLowerCase(), ChatFormat(globalFormatSec, group))
                LogHelper.debug(this, "Loaded global format for group '${group.toLowerCase()}'")
            }
        }

        // Set default chat format if not already set
        if (!globalFormats.containsKey("default")) {
            globalFormats.put("default", ChatFormat("{CHCOLOR}[{CHTAG}]{PREFIX}{DISPNAME}{SUFFIX}&f: {MESSAGE}", true))
        }

        /* Load channels */
        defaultChannel = config.getString("default channel")

        val channelDir = dataFolder
        if (!channelDir.exists()) {
            channelDir.mkdirs()
        }

        var channelFiles = channelDir.listFiles { file -> file.name.endsWith(".channel.yml") }

        if (channelFiles.isEmpty()) {
            LogHelper.info(this, "No channels found in channel directory. Creating a default channel file.")
            plugin.saveResource("channels" + File.separator + "default.channel.yml", true)
        }

        // Refresh channel files for new default file
        channelFiles = channelDir.listFiles { file -> file.name.endsWith(".channel.yml") }

        val newLoadedChannels: MutableList<String> = ArrayList()
        for (file in channelFiles) {
            val yaml = YamlFileManager(file)
            val name = yaml.config.getString("name")?.toLowerCase() ?: ""
            if (name.isNullOrEmpty()) {
                LogHelper.warning(this, "Invalid channel file '${file.name}': no name defined")
                continue
            }

            if (channels.containsKey(name)) {
                channels[name]!!.reload(this, yaml.config)
                newLoadedChannels.add(name)
                continue
            }

            val channel: Channel
            try {
                channel = StandardChannel(name)
                channel.reload(this, yaml.config)
            } catch (ex: Exception) {
                LogHelper.severe(this, "An exception occurred while loading channel '$name'", ex)
                continue
            }

            newLoadedChannels.add(name)
            channels.put(name, channel)

            if (!loadedChannels.contains(name)) {
                registerPermissions(name)
            }
        }

        // Load default channel
        if (defaultChannel.isNotEmpty() && !channels.containsKey(defaultChannel.toLowerCase())) {
            LogHelper.warning(this, "The default channel '$defaultChannel' is not loaded.")
        }

        val chatterModule = FlexPlugin.getPluginModule(FlexChat::class, ChatterModule::class)!!

        // Names in loadedChannels are lower case
        for (channel in loadedChannels) {
            if (!newLoadedChannels.contains(channel)) {
                // Remove chatters from obsolete channel instances
                for (chatter in chatterModule.chatters.values) {
                    for (inst in chatter.instances.keys) {
                        if (inst.channel.name == channel) {
                            chatter.removeInstance(inst)
                        }
                    }
                }

                // Remove permissions
                unregisterPermissions(channel)
                muteRunnables.remove(channel.toLowerCase())?.cancel()

                LogHelper.debug(this, "Removing obsolete channel '$channel'")
            }
        }

        loadedChannels.clear()
        loadedChannels.addAll(newLoadedChannels)

        LogHelper.info(this, "Loaded ${loadedChannels.size} channel(s)")
    }

    private fun registerPermissions(channelName: String) {
        val pluginManager = Bukkit.getPluginManager()
        val s = channelName.toLowerCase()

        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.AUTOJOIN, s).node, PermissionDefault.FALSE))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.INFO, s).node, PermissionDefault.TRUE))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.JOIN, s).node, PermissionDefault.OP))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.LEAVE, s).node, PermissionDefault.OP))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.CHAT, s).node, PermissionDefault.OP))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.VIEW, s).node, PermissionDefault.TRUE))
        pluginManager.addPermission(Permission(PermissionNode.buildVariableName(PermissionNodes.SPY, s).node, PermissionDefault.TRUE))
    }

    private fun unregisterPermissions(channelName: String) {
        val pluginManager = Bukkit.getPluginManager()
        val s = channelName.toLowerCase()

        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.AUTOJOIN, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.INFO, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.JOIN, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.LEAVE, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.CHAT, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.VIEW, s).node))
        pluginManager.removePermission(pluginManager.getPermission(PermissionNode.buildVariableName(PermissionNodes.SPY, s).node))
    }

    override fun registerChannel(channel: Channel, defaultConfig: InputStream?): Boolean {
        return false
    }

    override fun unregisterChannel(channel: Channel): Boolean {
        return false
    }

    override fun getDefaultChannel(): Channel {
        return channels[defaultChannel]!!
    }

    override fun getChannel(name: String): Channel? {
        return channels[name.toLowerCase()]
    }

    override fun getDefaultChannelFormat(): ChatFormat {
        return globalFormats["default"]!!
    }

}

private class ChannelUnmuteRunnable : BukkitRunnable {

    val end: Long
    val channel: Channel

    constructor(length: Int, channel: Channel) {
        end = System.currentTimeMillis() + length * 1000L
        this.channel = channel
    }

    fun getSecondsLeft(): Int = ((end - System.currentTimeMillis()) / 1000L).toInt()

    override fun run() {
        if (!channel.isMuted) {
            return
        }

        channel.isMuted = false
        channel.sendMessage(Message.get(FlexChat::class, "alerts_channel.unmuted",
                channel.color.toString(), channel.name))
    }

}
