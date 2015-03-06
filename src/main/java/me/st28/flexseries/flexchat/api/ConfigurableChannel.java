package me.st28.flexseries.flexchat.api;

import org.bukkit.ChatColor;

/**
 * Represents a {@link me.st28.flexseries.flexchat.api.Channel} that can be configured via the standard channel directory.
 */
public abstract class ConfigurableChannel extends Channel {

    ChannelData data;

    public ConfigurableChannel(String identifier) {
        super(identifier);
    }

    @Override
    public void save() {
        data.save();
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getShortName() {
        return data.getShortName() != null ? data.getShortName() : data.getName();
    }

    @Override
    public ChatColor getColor() {
        return data.getColor();
    }

    @Override
    public boolean hasOwnPermissions() {
        return data.getOption("permissions", true);
    }

    /**
     * @return the configuration data instance for this channel.
     */
    public final ChannelData getData() {
        return data;
    }

}