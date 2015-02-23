package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChannelEvent extends Event {

    private final static HandlerList handlerList = new HandlerList();

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

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}