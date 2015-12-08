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
package me.st28.flexseries.flexchat.api.format;

import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public final class StandardChatFormat extends ChatFormat {

    private String format;
    private boolean inherit;

    public StandardChatFormat(String format, boolean inherit) {
        Validate.notNull(format, "Format cannot be null.");
        this.format = format;
        this.inherit = inherit;
    }

    @Override
    public boolean shouldInheritGroup() {
        return inherit;
    }

    @Override
    public String getFormattedResult(Chatter chatter, Channel channel) {
        String returnFormat = format;

        // Replacements
        Map<String, String> cachedReplacements = new HashMap<>();

        for (ChatVariable variable : ChatFormat.VARIABLES.values()) {
            String key = variable.getReplaceKey();
            if (!cachedReplacements.containsKey(key)) {
                String replacement = variable.getReplacement(chatter, channel);
                if (replacement == null) {
                    replacement = "";
                }
                cachedReplacements.put(key, replacement);
            }

            returnFormat = returnFormat.replace(key, cachedReplacements.get(key));
        }

        returnFormat = ChatColor.translateAlternateColorCodes('&', returnFormat);

        return returnFormat;
    }

}