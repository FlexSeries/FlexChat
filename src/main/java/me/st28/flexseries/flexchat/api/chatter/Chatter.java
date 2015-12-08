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
package me.st28.flexseries.flexchat.api.chatter;

import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Chatter {

    private final String identifier;

    private ChannelInstance activeInstance;
    private final Map<ChannelInstance, Long> instances = new HashMap<>();

    protected Chatter(String identifier) {
        this.identifier = identifier;
    }

    public void load(ConfigurationSection config) {
        ChannelManagerImpl channelManager = FlexPlugin.getGlobalModule(ChannelManagerImpl.class);

        // Load channel instances
        ConfigurationSection instanceSec = config.getConfigurationSection("instances");
        if (instanceSec != null) {
            for (String chName : instanceSec.getKeys(false)) {
                Channel channel = channelManager.getChannel(chName);
                if (channel == null) {
                    // Ignore invalid channel names from the config
                    continue;
                }

                List<String> instanceNames = instanceSec.getStringList(chName);
                if (!instanceNames.isEmpty()) {
                    for (String label : instanceNames) {
                        ChannelInstance instance = channel.getInstance(label);
                        if (instance != null) {
                            this.instances.put(instance, System.currentTimeMillis());
                        }
                    }
                } else {
                    Collection<ChannelInstance> applicableInstances = channel.getInstances(this);
                    if (applicableInstances != null && applicableInstances.size() == 1) {
                        // If this chatter only belongs to a single channel instance, put them in it
                        this.instances.put(applicableInstances.iterator().next(), System.currentTimeMillis());
                    }
                }
            }
        }

        // Load active channel+instance
        String actChName = config.getString("active.channel");
        String actInstName = config.getString("active.instance");

        if (actChName == null) {
            // No channel, don't continue
            return;
        }

        Channel channel = channelManager.getChannel(actChName);
        if (channel == null) {
            // Channel doesn't exist, don't continue
            return;
        }

        Collection<ChannelInstance> applicableInstances = channel.getInstances(this);
        if (applicableInstances != null && actInstName == null && applicableInstances.size() == 1) {
            activeInstance = applicableInstances.iterator().next();
        } else {
            activeInstance = channel.getInstance(actInstName);
        }

        if (!this.instances.containsKey(activeInstance)) {
            // Not actually in active instance, don't set it
            activeInstance = null;
        }

        // Add to instances
        for (ChannelInstance instance : this.instances.keySet()) {
            instance.addChatter(this);
        }
    }

    public void save(ConfigurationSection config) {
        Map<String, List<String>> toSave = new HashMap<>();
        for (ChannelInstance instance : instances.keySet()) {
            String chName = instance.getChannel().getName();

            if (!toSave.containsKey(chName)) {
                toSave.put(chName, new ArrayList<>());
            }

            toSave.get(chName).add(instance.getLabel());
        }

        ConfigurationSection instanceSec = config.createSection("instances");
        for (Entry<String, List<String>> entry : toSave.entrySet()) {
            instanceSec.set(entry.getKey(), entry.getValue());
        }

        if (activeInstance != null) {
            config.set("active.channel", activeInstance.getChannel().getName());
            config.set("active.instance", activeInstance.getLabel());
        } else {
            config.set("active.channel", null);
            config.set("active.instance", null);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract String getName();

    public String getDisplayName() {
        return getName();
    }

    public List<ChannelInstance> getInstances() {
        List<ChannelInstance> list = new ArrayList<>(instances.keySet());

        Collections.sort(list, (o1, o2) -> instances.get(o2).compareTo(instances.get(o1)));

        return list;
    }

    public int getInstanceCount(Channel channel) {
        int count = 0;
        for (ChannelInstance instance : instances.keySet()) {
            if (instance.getChannel() == channel) {
                count++;
            }
        }
        return count;
    }

    public boolean isInInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");
        return instances.containsKey(instance);
    }

    public boolean addInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        boolean result = instances.put(instance, System.currentTimeMillis()) == null;

        if (!instance.containsChatter(this)) {
            instance.addChatter(this);
        }

        return result;
    }

    public boolean removeInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        boolean result = instances.remove(instance) != null;

        if (instance.containsChatter(this)) {
            instance.removeChatter(this);
        }

        if (activeInstance == instance) {
            activeInstance = null;

            if (!instances.isEmpty()) {
                activeInstance = getInstances().get(0);
            }
        }

        return result;
    }

    public ChannelInstance getActiveInstance() {
        return activeInstance;
    }

    public boolean setActiveInstance(ChannelInstance instance) {
        if (instance == null) {
            if (this.activeInstance == null) {
                return false;
            } else {
                this.activeInstance = null;
                return true;
            }
        }

        if (!instances.containsKey(instance)
                || this.activeInstance == instance
                || !instance.getChannel().getAllInstances(this).contains(instance))
        {
            return false;
        }

        this.activeInstance = instance;
        instances.put(instance, System.currentTimeMillis());
        return true;
    }

    public abstract boolean hasPermission(PermissionNode permission);

    public abstract void sendMessage(String message);

    public abstract void sendMessage(MessageReference message);

}