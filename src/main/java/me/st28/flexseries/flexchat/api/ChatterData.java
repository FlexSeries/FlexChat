package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

public final class ChatterData {

    final YamlFileManager file;

    /**
     * The currently active channel the chatter is in. Can be null.
     */
    String activeChannel;

    /**
     * The channels the chatter is in.<br />
     * <b>Structure:</b> <code>channel name, last active time</code>
     */
    final Map<String, Long> channels = new HashMap<>();

    ChatterData(YamlFileManager file) {
        this.file = file;
        FileConfiguration config = file.getConfig();

        activeChannel = config.getString("activeChannel");

        ConfigurationSection channelSec = config.getConfigurationSection("channels");
        if (channelSec != null) {
            for (String identifier : channelSec.getKeys(false)) {
                channels.put(identifier, channelSec.getLong(identifier));
            }
        }
    }

    public final void refreshChannels() {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);

        Iterator<String> iterator = channels.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();

            Channel channel = channelManager.getChannel(next);
            if (channel == null) {
                iterator.remove();
            }
        }

        if (activeChannel != null && channelManager.getChannel(activeChannel) == null) {
            Channel nextChannel = getNextChannel();
            if (nextChannel == null) {
                activeChannel = null;
            } else {
                activeChannel = nextChannel.getIdentifier();
            }
        }
    }

    public void save() {
        FileConfiguration config = file.getConfig();

        config.set("activeChannel", activeChannel);

        ConfigurationSection channelSec = config.createSection("channels");
        for (Entry<String, Long> entry : channels.entrySet()) {
            channelSec.set(entry.getKey(), entry.getValue());
        }

        file.save();
    }

    public final Channel getActiveChannel() {
        return activeChannel == null ? null : FlexPlugin.getRegisteredModule(ChannelManager.class).getChannel(activeChannel);
    }

    public final Collection<Channel> getChannels() {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);

        List<Channel> returnList = new ArrayList<>();

        for (String identifier : channels.keySet()) {
            returnList.add(channelManager.getChannel(identifier));
        }

        Collections.sort(returnList, new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                return channels.get(o2.getIdentifier()).compareTo(channels.get(o1.getIdentifier()));
            }
        });

        return returnList;
    }

    public final Channel getNextChannel() {
        if (channels.isEmpty()) {
            return null;
        } else {
            return ((List<Channel>) getChannels()).get(0);
        }
    }

}