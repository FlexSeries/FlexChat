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
package me.st28.flexseries.flexchat.api.channel;

import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.api.format.ReferencedChatFormat;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.permission.PermissionHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class Channel {

    private String name;
    private String fileName;
    private String tag;

    private ChatColor color;
    private int radius;

    private String logFormat;

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

        color = ChatColor.valueOf(config.getString("color", "WHITE").toUpperCase());

        logFormat = config.getString("log format", null);
        if (logFormat != null && logFormat.equals("")) {
            logFormat = null;
        }

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

    public void save() {}

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName == null ? name : fileName;
    }

    public String getTag() {
        return tag;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getRadius() {
        return radius;
    }

    public Collection<ChatFormat> getChatFormats() {
        return Collections.unmodifiableCollection(formats.values());
    }

    public ChatFormat getChatFormat(String group) {
        return formats.get(group == null ? "default" : group.toLowerCase());
    }

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
     * @return A {@link ChannelInstance} with the given name.
     */
    public ChannelInstance getInstance(String name) {
        Validate.notNull(name, "Name cannot be null.");
        for (ChannelInstance instance : getInstances()) {
            if (instance.getDisplayName().equalsIgnoreCase(name) || instance.getLabel().equalsIgnoreCase("label")) {
                return instance;
            }
        }
        return null;
    }

    /**
     * @return The {@link ChannelInstance}s a particular {@link Chatter} belongs in.
     */
    public abstract List<ChannelInstance> getInstances(Chatter chatter);

}