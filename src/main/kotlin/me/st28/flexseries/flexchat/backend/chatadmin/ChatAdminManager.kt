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
package me.st28.flexseries.flexchat.backend.chatadmin

import me.st28.flexseries.flexchat.FlexChat
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import java.io.File
import java.util.*

class ChatAdminManager : FlexModule<FlexChat> {

    companion object {

        val COMMAND_LABEL_PATTERN: Regex = Regex("(?i)^(/[^ ]+).+")

    }

    private var commandOutput: String = ""
    private val spyCommands: MutableList<String> = ArrayList()

    private var channelOutput: String = ""
    private var instanceOutput: String = ""

    private val spies: MutableMap<UUID, SpySettings> = HashMap()
    private var spyFile: YamlFileManager? = null

    constructor(plugin: FlexChat) : super(plugin, "chat-admin", "Manages chat administration features")

    override fun handleEnable() {
        spyFile = YamlFileManager(dataFolder.path + File.separator + "chatSpies.yml")
    }

    private fun loadPlayer(uuid: UUID) {
        spies.put(uuid, SpySettings(spyFile!!.config.getConfigurationSection(uuid.toString())))
    }

}