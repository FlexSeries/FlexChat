package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import org.bukkit.event.Event;

public abstract class ChannelEvent extends Event {

    private final Channel channel;
    private final Chatter chatter;

    /**
     * Represents a channel event.
     *
     * @param channel The channel involved.
     * @param chatter The chatter involved.
     */
    public ChannelEvent(Channel channel, Chatter chatter) {
        this.channel = channel;
        this.chatter = chatter;
    }

    public final Channel getChannel() {
        return channel;
    }

    public final Chatter getChatter() {
        return chatter;
    }

}