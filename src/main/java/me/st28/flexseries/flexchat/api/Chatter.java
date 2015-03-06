package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.events.ChannelActiveSetEvent;
import me.st28.flexseries.flexchat.api.events.ChannelJoinEvent;
import me.st28.flexseries.flexchat.api.events.ChannelLeaveEvent;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.util.Collection;

/**
 * Represents something with the ability to chat.
 */
public abstract class Chatter {

    /**
     * A unique identifier for the chatter.
     */
    private final String identifier;

    ChatterData data;

    public Chatter(String identifier) {
        Validate.notNull(identifier, "Identifier cannot be null.");

        this.identifier = identifier;
    }

    /*
     * @return a unique identifier for the chatter.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return the name of the chatter.
     */
    public abstract String getName();

    /**
     * @return the name to display for the chatter instead of their normal name.<br />
     *         Should never return null. If there isn't a display name set, should return the normal name.
     */
    public String getDisplayName() {
        return getName();
    }

    public final Channel getActiveChannel() {
        return data.getActiveChannel();
    }

    public final DynamicResponse setActiveChannel(Channel channel) {
        if (channel == null) {
            if (getActiveChannel() == null) {
                return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_active_already_none"));
            } else {
                data.activeChannel = null;
                return new DynamicResponse(true);
            }
        }

        DynamicResponse response = null;
        if (!this.getChannels().contains(channel)) {
            response = addChannel(channel);
            if (!response.isSuccess()) {
                return response;
            }
        }

        if (this.getActiveChannel() == channel) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_active_already_set", new QuickMap<>("{CHANNEL}", channel.getName()).getMap()));
        }

        data.activeChannel = channel.getIdentifier();
        Bukkit.getPluginManager().callEvent(new ChannelActiveSetEvent(channel, this));

        if (response != null) {
            return new DynamicResponse(true, response.getMessages()[0], MessageReference.create(FlexChat.class, "notices.channel_active_set", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        } else {
            return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_active_set", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        }
    }

    public final Collection<Channel> getChannels() {
        return data.getChannels();
    }

    public DynamicResponse addChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");

        DynamicResponse response = channel.addChatter(this, false);
        if (!response.isSuccess()) {
            return response;
        }

        data.channels.put(channel.getIdentifier(), System.currentTimeMillis());
        Bukkit.getPluginManager().callEvent(new ChannelJoinEvent(channel, this));

        return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_joined", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
    }

    public DynamicResponse removeChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");

        DynamicResponse response = channel.removeChatter(this, false);
        if (!response.isSuccess()) {
            return response;
        }

        data.channels.remove(channel.getIdentifier());
        Bukkit.getPluginManager().callEvent(new ChannelLeaveEvent(channel, this));

        DynamicResponse activeResponse = null;
        if (getActiveChannel() == channel) {
            if (!data.channels.isEmpty()) {
                activeResponse = setActiveChannel(data.getNextChannel());
            } else {
                setActiveChannel(null);
            }
        }

        if (activeResponse != null && activeResponse.isSuccess()) {
            return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_left", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()), activeResponse.getMessages()[0]);
        } else {
            return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_left", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        }
    }

    /**
     * Sends a message to the chatter.
     */
    public abstract void sendMessage(MessageReference message);

}