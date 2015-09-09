/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexchat.logging;

import me.st28.flexseries.flexchat.FlexChat;
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

}