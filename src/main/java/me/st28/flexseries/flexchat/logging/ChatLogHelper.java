/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexchat.logging;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexlib.log.LogHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChatLogHelper {

    private static boolean isInitialized = false;

    public static final Logger CHAT_LOGGER = Logger.getLogger(FlexChat.class.getCanonicalName());

    private static boolean logToBukkit = true;

    public static void init(FlexChat plugin) {
        if (isInitialized) return;

        CHAT_LOGGER.setLevel(Level.INFO);
        CHAT_LOGGER.setParent(Bukkit.getLogger());
        CHAT_LOGGER.setUseParentHandlers(false);

        File logDir = new File(plugin.getDataFolder() + File.separator + "logs");
        logDir.mkdirs();

        String fileName = logDir.getAbsolutePath() + File.separator + "flexchat.%g.%u.log";
        try {
            FileHandler handler = new FileHandler(fileName, 524288, 1000, true);

            handler.setFormatter(new ChatLogFormatter());
            CHAT_LOGGER.addHandler(handler);
        } catch (IOException ex) {
            LogHelper.severe(plugin, "Unable to create log handler.", ex);
        }

        isInitialized = true;
    }

    public static void reload(FileConfiguration config) {
        logToBukkit = config.getBoolean("logging.log to bukkit", true);
    }

    public static void log(String message) {
        log(message, null);
    }

    public static void log(String message, String channel) {
        if (channel != null) {
            CHAT_LOGGER.log(Level.INFO, "[[" + channel + "]] " + message);
        } else {
            CHAT_LOGGER.log(Level.INFO, message);
        }

        if (logToBukkit) {
            Bukkit.getLogger().log(Level.INFO, "[CHAT] " + message);
        }
    }

    public static void log(ChannelInstance instance, String message) {
        String channel = instance.getChannel().getName();
        String instanceName = instance.getDisplayName();

        CHAT_LOGGER.log(Level.INFO, "[[" + channel + (instanceName == null ? "" : (":" + instanceName)) + "]] " + message);

        if (logToBukkit) {
            Bukkit.getLogger().log(Level.INFO, "[CHAT] " + message);
        }
    }

}