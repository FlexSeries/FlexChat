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
package me.st28.flexseries.flexchat.api.chatter;

import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.Map.Entry;

public abstract class Chatter {

    private final String identifier;

    private ChannelInstance activeInstance;
    private final Set<ChannelInstance> instances = new HashSet<>();

    protected Chatter(String identifier) {
        this.identifier = identifier;
    }

    public void load(ConfigurationSection config) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);

        ConfigurationSection instanceSec = config.getConfigurationSection("instances");
        if (instanceSec != null) {
            for (String chName : instanceSec.getKeys(false)) {
                Channel channel = channelManager.getChannel(chName);

                Map<String, ChannelInstance> curInstances = new HashMap<>();
                for (ChannelInstance instance : channel.getInstances(this)) {
                    curInstances.put(instance.getLabel(), instance);
                }

                if (instanceSec.isSet(chName) && !instanceSec.getStringList(chName).isEmpty()) {
                    for (String instanceLabel : instanceSec.getStringList(chName)) {
                        if (curInstances.containsKey(instanceLabel)) {
                            instances.add(curInstances.get(instanceLabel));
                        }
                    }
                } else if (curInstances.size() == 1) {
                    instances.add(channel.getInstances(this).get(0));
                }
            }
        }

        String actChName = config.getString("active.channel");
        String actInstName = config.getString("active.instance");

        if (actChName == null) {
            return;
        }

        Channel channel = channelManager.getChannel(actChName);
        if (channel == null) {
            return;
        }

        List<ChannelInstance> instances = channel.getInstances(this);
        if (actInstName == null && instances.size() == 1) {
            activeInstance = instances.get(0);
        } else if (actInstName != null) {
            for (ChannelInstance instance : instances) {
                if (instance.getLabel().equals(actInstName)) {
                    activeInstance = instance;
                }
            }
        }

        if (!this.instances.contains(activeInstance)) {
            activeInstance = null;
        }

        // Add to instances
        for (ChannelInstance instance : this.instances) {
            instance.addChatter(this);
        }
    }

    public void save(ConfigurationSection config) {
        Map<String, List<String>> toSave = new HashMap<>();
        for (ChannelInstance instance : instances) {
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
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract String getName();

    public String getDisplayName() {
        return getName();
    }

    public Collection<ChannelInstance> getInstances() {
        return Collections.unmodifiableCollection(instances);
    }

    public boolean isInInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");
        return instances.contains(instance);
    }

    public boolean addInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        boolean result = instances.add(instance);

        if (!instance.containsChatter(this)) {
            instance.addChatter(this);
        }

        return result;
    }

    public boolean removeInstance(ChannelInstance instance) {
        Validate.notNull(instance, "Instance cannot be null.");

        boolean result = instances.remove(instance);

        if (instance.containsChatter(this)) {
            instance.removeChatter(this);
        }

        if (activeInstance == instance) {
            activeInstance = null;
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

        if (!instances.contains(instance)
                || this.activeInstance == instance
                || !instance.getChannel().getInstances(this).contains(instance)
        ) {
            return false;
        }

        this.activeInstance = instance;
        return true;
    }

    public abstract boolean hasPermission(PermissionNode permission);

    public abstract void sendMessage(String message);

    public abstract void sendMessage(MessageReference message);

}