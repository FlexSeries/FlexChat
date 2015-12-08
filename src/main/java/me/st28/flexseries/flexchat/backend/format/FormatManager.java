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
package me.st28.flexseries.flexchat.backend.format;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterConsole;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexlib.permission.PermissionHelper;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class FormatManager extends FlexModule<FlexChat> {

    private final Map<String, String> messageFormats = new HashMap<>();

    public FormatManager(FlexChat plugin) {
        super(plugin, "formats", "Handles chat formats", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleReload() {
        final ConfigurationSection config = getConfig();

        messageFormats.clear();
        final ConfigurationSection messageSec = config.getConfigurationSection("message formats");
        if (messageSec != null) {
            for (String group : messageSec.getKeys(false)) {
                messageFormats.put(group.toLowerCase(), StringEscapeUtils.unescapeJava(messageSec.getString(group)));
            }
        }

        if (!messageFormats.containsKey("default")) {
            messageFormats.put("default", "&f[&7{SENDER} &f\u27A1 &7{RECEIVER}&f] &7{MESSAGE}");
        }
    }

    public String getFormat(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");

        if (chatter instanceof ChatterConsole) {
            return messageFormats.containsKey("console") ? messageFormats.get("console") : messageFormats.get("default");
        }

        return messageFormats.get(PermissionHelper.getTopGroup(((ChatterPlayer) chatter).getPlayer(), new ArrayList<>(messageFormats.keySet()), "default"));
    }

    public String formatMessage(Chatter sender, String message) {
        String finalMessage = getFormat(sender);

        Map<String, String> cachedReplacements = new HashMap<>();

        for (ChatVariable variable : ChatFormat.VARIABLES.values()) {
            String key = variable.getReplaceKey();

            if (!finalMessage.contains(key)) {
                continue;
            }

            if (!cachedReplacements.containsKey(key)) {
                String replacement = variable.getReplacement(sender, null);
                if (replacement == null) {
                    replacement = "";
                }
                cachedReplacements.put(key, replacement);
            }

            finalMessage = finalMessage.replace(key, cachedReplacements.get(key));
        }

        return ChatColor.translateAlternateColorCodes('&', finalMessage).replace("{MESSAGE}", ChatFormat.applyApplicableChatColors(sender, message));
    }

}