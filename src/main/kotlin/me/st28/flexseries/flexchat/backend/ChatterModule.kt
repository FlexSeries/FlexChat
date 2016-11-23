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
import me.st28.flexseries.flexchat.api.ChatProvider
import me.st28.flexseries.flexchat.api.Chatter
import me.st28.flexseries.flexchat.api.ChatterManager
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import java.io.File

class ChatterModule(plugin: FlexChat) : FlexModule<FlexChat>(plugin, "chatters", "Manages chatters"), ChatterManager {

    override fun loadChatter(provider: ChatProvider, identifier: String): Chatter {
        LogHelper.debug(this, "Loading chatter '$identifier'")

        val providerFolder = File(dataFolder.path + File.separator + provider.name)
        providerFolder.mkdirs()

        val file = YamlFileManager(providerFolder.path + File.separator + identifier + ".yml")

        val chatter = Chatter(provider, identifier, file)

        if (file.isEmpty()) {
            // New, add to default channel + instance
        }

        // TODO: Load channels and instances
        val channelSec = file.config.getConfigurationSection("channels")

        return chatter
    }

    override fun saveChatter(chatter: Chatter) {
    }

    override fun unloadChatter(chatter: Chatter, save: Boolean) {
        LogHelper.debug(this, "Unloading chatter '${chatter.identifier}'")

        if (save) {
            saveChatter(chatter)
        }

        chatter.provider.chatters.remove(chatter.identifier)
    }

}
