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
package me.st28.flexseries.flexchat.backend

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.PermissionNodes
import me.st28.flexseries.flexchat.api.ChatManager
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexchat.api.channel.ChannelInstance
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.util.ChatColorUtils
import me.st28.flexseries.flexlib.util.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import java.util.*

class ChatModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "chat", "Manages chat"), ChatManager {

    internal val providers: MutableMap<String, ChatProvider> = HashMap()

    private lateinit var channelPrefix: String
    private var channelPrefixEnabled: Boolean = true
    private var channelPrefixForce: Boolean = false

    private val defaultFormats: MutableMap<String, String> = HashMap()
    private val messageFormats: MutableMap<String, String> = HashMap()

    override fun handleReload() {
        /* Reload chat configuration */

        // Default format
        defaultFormats.clear()
        config.getConfigurationSection("default formats")?.getValues(false)?.forEach {
            defaultFormats.put(it.key, it.value as String)
        }

        if (!defaultFormats.containsKey("default")) {
            LogHelper.info(this, "No default format found, inserting fallback")
            defaultFormats.put("default", "<{SENDER}> {MESSAGE}")
        }

        // Channel prefix config
        channelPrefix = config.getString("channel prefix.format", "")
        channelPrefixEnabled = config.getBoolean("channel prefix.enabled", true)
        channelPrefixForce = config.getBoolean("channel prefix.force state", false)

        /* Load message formats */
        messageFormats.clear()
        config.getConfigurationSection("message formats")?.getValues(false)?.forEach {
            messageFormats.put(it.key, it.value as String)
        }

        if (!messageFormats.containsKey("default")) {
            LogHelper.info(this, "No default message format found, inserting fallback")
            messageFormats.put("default", "&f[&7{SENDER} &f\u27A1 &7{RECEIVER}&f] &7{MESSAGE}".translateColorCodes())
        }

        /* Reload providers */
        for (provider in providers.values) {
            val providerConfig = config.getConfigurationSection(provider.name)
            provider.reload(providerConfig)
        }
    }

    override fun handleDisable() {
        /* Disable chat providers */
        providers.values.forEach(ChatProvider::disable)
    }

    override fun registerProvider(provider: ChatProvider): Boolean {
        val key = provider.name.toLowerCase()
        if (providers.containsKey(key)) {
            return false
        }

        providers.put(key, provider)

        val providerConfig = config.getConfigurationSection(provider.name)
        provider.enable(providerConfig)

        if (provider is Listener) {
            Bukkit.getPluginManager().registerEvents(provider, provider.plugin)
        }

        LogHelper.info(this, "Registered chat provider '$key' (${provider.javaClass.canonicalName})")
        return true
    }

    override fun getDefaultChatFormat(provider: ChatProvider): String {
        return defaultFormats[provider.name] ?: defaultFormats["default"]!!
    }

    override fun processFormat(chatter: Chatter, instance: ChannelInstance, format: String): String {
        val sb = StringBuilder()

        // TODO: Check channel-specific prefix setting
        if (channelPrefixEnabled || channelPrefixForce) {
            sb.append(channelPrefix)
        }

        return sb.append(format).toString()
                .replace("{PROVIDER}", chatter.provider.name)
                .replace("{NAME}", chatter.name)
                .replace("{DISPNAME}", chatter.displayName)
                .replace("{CHTAG}", instance.channel.tag)
                .replace("{CHNAME}", instance.channel.name)
                .replace("{CHCOLOR}", instance.channel.color.toString())
    }

    override fun sendMessage(chatter: Chatter, instance: ChannelInstance, format: String?, message: String) {
        // TODO: Perform checks
        sendMessageUnsafe(chatter, instance, format, message)
    }

    override fun sendMessageUnsafe(chatter: Chatter, instance: ChannelInstance, format: String?, message: String) {
        var applicableFormat = format ?: getDefaultChatFormat(chatter.provider)

        applicableFormat = processFormat(chatter, instance, applicableFormat)
        applicableFormat = chatter.provider.processFormat(chatter, instance, applicableFormat).translateColorCodes()

        var finalMessage = message

        if (chatter.hasPermission(PermissionNodes.COLOR)) {
            finalMessage = ChatColorUtils.applyColors(finalMessage)
        }

        if (chatter.hasPermission(PermissionNodes.FORMAT)) {
            finalMessage = ChatColorUtils.applyFormats(finalMessage)
        }

        if (chatter.hasPermission(PermissionNodes.MAGIC)) {
            finalMessage = ChatColorUtils.applySingle(finalMessage, ChatColor.MAGIC)
        }

        applicableFormat = applicableFormat.replace("{MESSAGE}", finalMessage)
        instance.getVisibleChatters(chatter).forEach { it.sendMessage(applicableFormat) }
    }

}
