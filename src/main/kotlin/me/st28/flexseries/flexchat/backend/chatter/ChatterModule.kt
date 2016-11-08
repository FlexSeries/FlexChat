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
package me.st28.flexseries.flexchat.backend.chatter

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.ChatterManager
import me.st28.flexseries.flexchat.api.chatter.ConsoleChatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatterModule : FlexModule<FlexChat>, ChatterManager, Listener {

    internal val chatters: MutableMap<String, Chatter> = ConcurrentHashMap()

    private lateinit var storage: Storage

    constructor(plugin: FlexChat) : super(plugin, "chatters", "Manages player chatter data")

    override fun handleEnable() {
        /* Load console chatter */
        val consoleChatter = ConsoleChatter()
        chatters.put(consoleChatter.name, consoleChatter)

        val config = getConfig()

        /* Load storage settings */
        val storageSec = config.getConfigurationSection("storage")
        val storageType = storageSec.getString("type")
        storage = when (storageType) {
            //"sqlite" -> Storage_SQLite()
            "yaml" -> Storage_YAML()
            else -> throw IllegalArgumentException("Invalid storage type '$storageType'")
        }
        LogHelper.info(this, "Using $storageType for storage")
        storage.enable(this, storageSec.getConfigurationSection(storageType) ?: storageSec.createSection(storageType))
    }

    override fun handleReload(isFirstReload: Boolean) {
    }

    override fun handleSave(async: Boolean, isFinalSave: Boolean) {
        SchedulerUtils.runAsap(plugin, {
            for (chatter in chatters.values) {
                saveChatter(chatter)
            }
        }, async)
    }

    private fun saveChatter(chatter: Chatter) {
        if (chatter is PlayerChatter) {
            storage.save(chatter)
        }
    }

    private fun loadPlayer(uuid: UUID) {
        val identifier = uuid.toString()
        if (!chatters.containsKey(identifier)) {
            chatters.put(identifier, storage.load(identifier))
        }
    }

    private fun unloadPlayer(player: Player) {
        val chatter = chatters.remove(player.uniqueId.toString())
        if (chatter != null) {
            chatter.instances.keys.forEach { it.removeOfflineChatter(chatter) }
            storage.unload(chatter)
        }
    }

    override fun getChatter(sender: CommandSender): Chatter? {
        if (sender is Player) {
            return chatters[sender.uniqueId.toString()]
        }
        return chatters[ConsoleChatter.NAME]
    }

    override fun getChatter(identifier: String): Chatter? = chatters[identifier]

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        loadPlayer(e.player.uniqueId)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        unloadPlayer(e.player)
    }

}