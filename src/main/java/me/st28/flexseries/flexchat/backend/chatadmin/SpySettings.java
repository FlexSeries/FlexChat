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
package me.st28.flexseries.flexchat.backend.chatadmin;

import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.Map.Entry;

public final class SpySettings {

    private boolean isEnabled;
    private final Set<String> channels = new HashSet<>();
    private final Map<String, Set<String>> instances = new HashMap<>();

    public SpySettings() {
        this.isEnabled = false;
    }

    public SpySettings(ConfigurationSection config) {
        if (config == null) {
            isEnabled = false;
            return;
        }

        this.isEnabled = config.getBoolean("enabled", false);

        channels.addAll(config.getStringList("channels"));

        ConfigurationSection instanceSec = config.getConfigurationSection("instances");
        if (instanceSec == null) {
            return;
        }

        for (String channel : instanceSec.getKeys(false)) {
            if (!instances.containsKey(channel.toLowerCase())) {
                instances.put(channel.toLowerCase(), new HashSet<>());
            }

            instances.get(channel.toLowerCase()).addAll(instanceSec.getStringList(channel));
        }
    }

    public void save(ConfigurationSection config) {
        config.set("enabled", isEnabled);

        config.set("channels", new ArrayList<>(channels));

        ConfigurationSection instanceSec = config.createSection("instances");
        for (Entry<String, Set<String>> entry : instances.entrySet()) {
            List<String> list = new ArrayList<>(entry.getValue());
            if (list.isEmpty()) {
                continue;
            }

            instanceSec.set(entry.getKey(), list);
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean setEnabled(boolean isEnabled) {
        if (this.isEnabled == isEnabled) {
            return false;
        }

        this.isEnabled = isEnabled;
        return true;
    }

    public boolean containsChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");
        return channels.contains(channel.getName().toLowerCase());
    }

    public boolean addChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");
        return channels.add(channel.getName().toLowerCase());
    }

    public boolean removeChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");
        return channels.remove(channel.getName().toLowerCase());
    }

    public boolean containsInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        if (containsChannel(instance.getChannel())) {
            return true;
        };

        String channel = instance.getChannel().getName().toLowerCase();
        if (!instances.containsKey(channel)) {
            return false;
        }

        String label = instance.getLabel();
        if (label == null) {
            return containsChannel(instance.getChannel());
        }

        return instances.get(channel).contains(label.toLowerCase());
    }

    public boolean addInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        String channel = instance.getChannel().getName().toLowerCase();
        if (!instances.containsKey(channel)) {
            instances.put(channel, new HashSet<>());
        }

        String label = instance.getLabel();
        if (label == null) {
            return addChannel(instance.getChannel());
        }

        return instances.get(channel).add(label.toLowerCase());
    }

    public boolean removeInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        String channel = instance.getChannel().getName().toLowerCase();
        if (!instances.containsKey(channel)) {
            return false;
        }

        String label = instance.getLabel();
        if (label == null) {
            return removeChannel(instance.getChannel());
        }

        return instances.get(channel).remove(label.toLowerCase());
    }

}