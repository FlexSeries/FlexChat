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
    public String getFormattedResult(Chatter chatter, Channel channel, String message) {
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

        message = ChatFormat.applyApplicableChatColors(chatter, message);

        return returnFormat.replace("{MESSAGE}", message);
    }

}