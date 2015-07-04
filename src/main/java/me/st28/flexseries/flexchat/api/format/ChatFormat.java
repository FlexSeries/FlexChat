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
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.variables.VaultChatVariable;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.hook.hooks.VaultHook;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.util.ChatColorUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class ChatFormat {

    static {
        VARIABLES = new HashMap<>();

        ChatFormat.registerChatVariable(new ChatVariable("NAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return chatter.getName();
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("DISPNAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return chatter.getDisplayName();
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("WORLD") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (chatter instanceof ChatterPlayer) {
                    return ((ChatterPlayer) chatter).getPlayer().getLocation().getWorld().getName();
                }
                return null;
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("CHCOLOR") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return channel.getColor().toString();
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("CHTAG") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return channel.getTag();
            }
        });

        ChatFormat.registerChatVariable(new VaultChatVariable("GROUP") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                VaultHook vault = getVaultHook();
                if (vault == null || !(chatter instanceof ChatterPlayer)) {
                    return null;
                }

                return vault.getChat().getPrimaryGroup(null, ((ChatterPlayer) chatter).getPlayer());
            }
        });

        ChatFormat.registerChatVariable(new VaultChatVariable("PREFIX") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                VaultHook vault = getVaultHook();
                if (vault == null || !(chatter instanceof ChatterPlayer)) {
                    return null;
                }

                return vault.getChat().getPlayerPrefix(null, ((ChatterPlayer) chatter).getPlayer());
            }
        });

        ChatFormat.registerChatVariable(new VaultChatVariable("SUFFIX") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                VaultHook vault = getVaultHook();
                if (vault == null || !(chatter instanceof ChatterPlayer)) {
                    return null;
                }

                return vault.getChat().getPlayerSuffix(null, ((ChatterPlayer) chatter).getPlayer());
            }
        });
    }

    public static final Map<String, ChatVariable> VARIABLES;

    public static boolean registerChatVariable(ChatVariable variable) {
        Validate.notNull(variable, "Variable cannot be null.");
        String key = variable.getVariable().toLowerCase();

        if (VARIABLES.containsKey(key)) {
            return false;
        }

        VARIABLES.put(key, variable);
        return true;
    }

    public static ChatFormat getChatFormat(ChannelManagerImpl channelManager, ConfigurationSection section, String key) {
        Validate.notNull(section, "Section cannot be null.");
        Validate.notNull(key, "Key cannot be null.");

        Object object = section.get(key);

        if (object instanceof String) {
            String cast = (String) object;

            Matcher matcher = ChannelManagerImpl.GLOBAL_FORMAT_PATTERN.matcher(cast);
            if (matcher.matches()) {
                if (channelManager.getGlobalFormat(matcher.group(1)) == null) {
                    LogHelper.warning(channelManager, "Unknown format reference with group '" + cast + "', using default.");
                    return new ReferencedChatFormat(null);
                }

                return new ReferencedChatFormat(matcher.group(1));
            }
            return new StandardChatFormat(cast, true);
        } else if (object instanceof ConfigurationSection) {
            ConfigurationSection cast = (ConfigurationSection) object;

            return new StandardChatFormat(cast.getString("format"), cast.getBoolean("inherit", true));
        }
        throw new IllegalArgumentException("Invalid chat format section in configuration.");
    }

    public static String applyApplicableChatColors(Chatter chatter, String message) {
        if (chatter.hasPermission(PermissionNodes.COLOR)) {
            message = ChatColorUtils.colorizeString(message);
        }

        if (chatter.hasPermission(PermissionNodes.FORMAT)) {
            message = ChatColorUtils.formatString(message);
        }

        if (chatter.hasPermission(PermissionNodes.MAGIC)) {
            message = ChatColorUtils.magicfyString(message);
        }
        return message;
    }

    public boolean shouldInheritGroup() {
        return true;
    }

    public abstract String getFormattedResult(Chatter chatter, Channel channel);

}