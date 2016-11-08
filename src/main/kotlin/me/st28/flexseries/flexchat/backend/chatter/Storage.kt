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

import me.st28.flexseries.flexchat.api.FlexChatAPI
import me.st28.flexseries.flexchat.api.chatter.Chatter
import me.st28.flexseries.flexchat.api.chatter.ConsoleChatter
import me.st28.flexseries.flexchat.api.chatter.PlayerChatter
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import org.bukkit.configuration.ConfigurationSection
import java.io.File
import java.util.*

/**
 * The storage layer of the chatter manager.
 */
internal interface Storage {

    val name: String

    fun enable(module: ChatterModule, config: ConfigurationSection)
    fun disable()

    fun load(identifier: String): Chatter
    fun unload(chatter: Chatter)

    fun save(chatter: Chatter)

}

internal class Storage_YAML : Storage {

    override val name: String
        get() = "yaml"

    private lateinit var dataFolder: File

    private val files: MutableMap<String, YamlFileManager> = HashMap()

    override fun enable(module: ChatterModule, config: ConfigurationSection) {
        dataFolder = module.dataFolder
    }

    override fun disable() {
    }

    override fun load(identifier: String): Chatter {
        val isNew: Boolean
        if (!files.containsKey(identifier)) {
            files.put(identifier, YamlFileManager(dataFolder.path + File.separator + identifier + ".yml"))
            isNew = true
        } else {
            isNew = false
        }

        val config = files[identifier]!!.config

        if (isNew) {
            // Set default channel + instance
            val defaultChannel = FlexChatAPI.channelManager.getDefaultChannel()

            config.set("active.channel", defaultChannel.name)
            config.set("active.instance", "(default)")
            config.set("instances.${defaultChannel.name}", arrayListOf("(default)"))
        }

        var uuid: UUID? = null
        try {
            uuid = UUID.fromString(identifier)
        } catch (ex: IllegalArgumentException) { }

        val chatter: Chatter
        if (uuid != null) {
            chatter = PlayerChatter(uuid)
        } else {
            chatter = ConsoleChatter()
        }
        chatter.load(config)
        return chatter
    }

    override fun unload(chatter: Chatter) {
        files.remove(chatter.identifier)?.save()
    }

    override fun save(chatter: Chatter) {
        if (chatter !is PlayerChatter) {
            // TODO: Save console data?
            return
        }

        val file = files[chatter.identifier]!!
        chatter.save(file.config)
        file.save()
    }

}

/*internal class Storage_SQLite : Storage {

    override val name: String
        get() = "sqlite"

    private lateinit var connection: Connection

    private val queries: MutableMap<String, String> = HashMap()

    override fun enable(module: ChatterModule, config: ConfigurationSection) {
        val file = File(module.dataFolder.path + File.separator + "chatterData.yml")
        if (!file.exists()) {
            file.createNewFile()
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + file)

        /* Load Queries */
        module.getResource("sqlite-queries.yml").reader().use {
            val queryConfig = YamlConfiguration.loadConfiguration(it)

            // Load table names
            val tableNames: MutableMap<String, String> = HashMap()
            for ((key, value) in queryConfig.getConfigurationSection("tables").getValues(true)) {
                tableNames.put(key, value as String)
            }

            for ((nameKey, nameVal) in tableNames) {
                val sec = queryConfig.getConfigurationSection(nameKey)
                for ((key, value) in sec.getValues(true)) {
                    queries.put(
                            "$nameKey.$key",
                            (value as String).replace("{TABLE_$nameKey}", nameVal)
                    )
                }
            }

            // Setup tables
            for (table in tableNames.keys) {
                val ps = connection.prepareStatement(queries["$table.create_table"])
                try {
                    ps.executeUpdate()
                } finally {
                    ps?.close()
                }
            }
        }
    }

    override fun disable() {
        connection.close()
    }

    override fun save(chatter: Chatter) {
    }

    override fun load(uuid: UUID): Chatter {
    }

}*/
