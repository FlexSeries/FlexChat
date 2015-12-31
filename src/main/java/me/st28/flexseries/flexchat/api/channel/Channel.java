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
package me.st28.flexseries.flexchat.api.channel;

import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.api.format.ReferencedChatFormat;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.permission.PermissionHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A FlexChat chat channel.
 */
public abstract class Channel {

    private String name;
    private String fileName;
    private String tag;

    private String description;
    private ChatColor color;
    private int radius;

    private final Map<String, ChatFormat> formats = new LinkedHashMap<>();

    public Channel(String name) {
        this(name, null);
    }

    public Channel(String name, String fileName) {
        Validate.notNull(name, "Name cannot be null.");
        this.name = name;
        this.fileName = fileName;

        color = ChatColor.WHITE;
        radius = 0;
    }

    public void reload(ChannelManagerImpl channelManager, ConfigurationSection config) {
        name = config.getString("name");
        Validate.notNull(name, "Name cannot be null.");

        tag = config.getString("tag");
        if (tag != null && tag.equals("")) {
            tag = null;
        }

        description = config.getString("description", null);
        if (description != null && description.equals("")) {
            description = null;
        }

        color = ChatColor.valueOf(config.getString("color", "WHITE").toUpperCase());

        radius = config.getInt("chat radius", 0);
        if (radius < 0) {
            radius = 0;
        }

        formats.clear();
        ConfigurationSection formatSec = config.getConfigurationSection("formats");
        if (formatSec != null) {
            for (String group : formatSec.getKeys(false)) {
                ChatFormat format;
                try {
                    format = ChatFormat.getChatFormat(channelManager, formatSec, group);
                } catch (Exception ex) {
                    LogHelper.warning(channelManager, "Unable to load chat format '" + group + "'", ex);
                    continue;
                }
                formats.put(group, format);
            }
        }

        if (!formats.containsKey("default")) {
            formats.put("default",  new ReferencedChatFormat(null));
        }
    }

    /**
     * @return The name of the channel.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name of the file this channel's configuration is stored in.
     */
    public String getFileName() {
        return fileName == null ? name : fileName;
    }

    /**
     * @return The channel's short name.
     */
    public String getTag() {
        return tag;
    }

    /**
     * @return The description of the channel.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The channel's primary color.
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * @return The chat radius of the channel.<br />
     *         0 for a global channel.
     */
    public int getRadius() {
        return radius < 0 ? 0 : radius;
    }

    /**
     * @return An unmodifiable collection of all of the chat formats for this channel.
     */
    public Collection<ChatFormat> getChatFormats() {
        return Collections.unmodifiableCollection(formats.values());
    }

    /**
     * @return The chat format for a specified group for this channel.
     */
    public ChatFormat getChatFormat(String group) {
        return formats.get(group == null ? "default" : group.toLowerCase());
    }

    /**
     * @return The chat format for a specified {@link Chatter}.
     */
    public ChatFormat getChatFormat(Chatter chatter) {
        if (!(chatter instanceof ChatterPlayer)) {
            return getChatFormat((String) null);
        }
        return formats.get(PermissionHelper.getTopGroup(((ChatterPlayer) chatter).getPlayer(), new ArrayList<>(formats.keySet()), "default"));
    }

    /**
     * @return All of the {@link ChannelInstance}s this Channel has.
     */
    public abstract Collection<ChannelInstance> getInstances();

    /**
     * @return A {@link ChannelInstance} with the given name. Null input returns the default instance
     *         (assuming there is one).
     */
    public ChannelInstance getInstance(String name) {
        try {
            for (ChannelInstance instance : getInstances()) {
                if ((name == null && instance.getDisplayName() == null)
                        || (instance.getDisplayName() != null && (instance.getDisplayName().equalsIgnoreCase(name) || instance.getLabel().equalsIgnoreCase(name)))) {
                    return instance;
                }
            }
        } catch (NullPointerException ex) {
            // TODO: Temporary fix to attempt at solving NullPointerException
            return null;
        }
        return null;
    }

    /**
     * @return All of the instances a chatter is in, including ones they aren't normally in (from
     *         {@link #getInstances(Chatter)}).
     */
    public Collection<ChannelInstance> getAllInstances(Chatter chatter) {
        Set<ChannelInstance> returnSet = new HashSet<>();

        for (ChannelInstance instance : getInstances()) {
            if (instance.containsChatter(chatter)) {
                returnSet.add(instance);
            }
        }

        returnSet.addAll(getInstances(chatter));

        return returnSet;
    }

    /**
     * @return The {@link ChannelInstance}s a particular {@link Chatter} belongs in.
     */
    public abstract Collection<ChannelInstance> getInstances(Chatter chatter);

    /**
     * @return Custom information to show on the info command when it's executed for this channel.
     */
    public Map<String, String> getCustomInfo() {
        return new LinkedHashMap<>();
    }

}