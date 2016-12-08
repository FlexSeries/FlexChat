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
import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.ChatterManager
import me.st28.flexseries.flexlib.permission.withVariables
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class ChatterModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "chatters", "Manages chatters"), ChatterManager {

    internal val chatters: MutableMap<String, Chatter> = HashMap()

    override fun handleSave(async: Boolean) {
        chatters.values.forEach { saveChatter(it) }
    }

    private fun getChatterFile(chatter: Chatter): YamlFileManager {
        return YamlFileManager(dataFolder.path + File.separator + chatter.provider.name + File.separator + chatter.identifier + ".yml")
    }

    override fun registerChatter(chatter: Chatter) {
        chatters.put(chatter.identifier, chatter)

        val file = getChatterFile(chatter)
        val isNew: Boolean

        if (file.isEmpty()) {
            isNew = true
            chatter.data.set("isNew", true)
        } else {
            isNew = false
        }

        chatter.load(file.config)

        // Attempt to add chatter to autojoinable channels
        FlexChatAPI.channels.getChannels().forEach {
            val visible = it.getVisibleInstances(chatter)

            // If only one instance is visible and the chatter has permission to autojoin, add them.
            if (visible.size == 1 && chatter.hasPermission(PermissionNodes.AUTOJOIN.withVariables(it.name))) {
                chatter.addInstance(visible.first(), true)
            }
        }

        // Set active instance to default, if chatter is new
        if (isNew) {
            val defaultInstance = FlexChatAPI.channels.getDefaultChannel()?.getDefaultInstance()
            if (defaultInstance != null && defaultInstance.containsChatter(chatter)) {
                chatter.activeInstance = defaultInstance
            }
        }
    }

    override fun saveChatter(chatter: Chatter) {
        val file = getChatterFile(chatter)
        chatter.save(file.config)
        file.save()
    }

    override fun unregisterChatter(chatter: Chatter) {
        chatters.remove(chatter.identifier)
    }

    override fun getChatter(identifier: String): Chatter? {
        return chatters[identifier]
    }

    override fun getChatterByName(name: String): Chatter? {
        return chatters.values.firstOrNull { it.name.equals(name, true) }
    }

    override fun getChatter(sender: CommandSender): Chatter {
        return chatters[(sender as? Player)?.uniqueId?.toString() ?: "CONSOLE"]!!
    }

}
