package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.DynamicResponse;

import java.util.Collection;

/**
 * Represents something with the ability to chat.
 */
public abstract class Chatter {

    public final static String CONFIG_ACTIVE_CHANNEL = "activeChannel";
    public final static String CONFIG_CHANNELS = "channels";

    private final String identifier;

    public Chatter(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return a unique identifier for the chatter.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return the chatter's active chat channel.
     */
    public final Channel getActiveChannel() {
        return FlexPlugin.getRegisteredModule(ChannelManager.class).getChatterActiveChannel(this);
    }

    /**
     * @return A DynamicResponse representing true if the active channel was successfully set.
     */
    public final DynamicResponse setActiveChannel(Channel channel) {
        return FlexPlugin.getRegisteredModule(ChannelManager.class).setChatterActiveChannel(channel, this);
    }

    /**
     * @return a collection of all of the joined channels for the chatter.
     */
    public final Collection<Channel> getChannels() {
        return FlexPlugin.getRegisteredModule(ChannelManager.class).getChatterChannels(this);
    }

    /**
     * @return the name of the chatter, to appear in chat messages.
     */
    public abstract String getName();

    /**
     * @return the display name of the chatter.
     */
    public String getDisplayName() {
        return getName();
    }

    public abstract void sendMessage(String message);

    public void sendMessage(MessageReference message) {
        sendMessage(message.getPlainMessage());
    }

}