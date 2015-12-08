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
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.variables.VaultChatVariable;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.utils.ChatColorUtils;
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