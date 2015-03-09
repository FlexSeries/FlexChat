package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;
import org.bukkit.event.HandlerList;

/**
 * Fired when a chatter leaves a channel.
 */
public final class ChannelLeaveEvent extends ChannelEvent {

    private final static HandlerList handlerList = new HandlerList();

    public ChannelLeaveEvent(Channel channel, Chatter chatter) {
        super(channel, chatter);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}