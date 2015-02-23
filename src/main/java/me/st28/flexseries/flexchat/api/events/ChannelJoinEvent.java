package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;

/**
 * Fired when a chatter joins a channel.
 */
public final class ChannelJoinEvent extends ChannelEvent {

    public ChannelJoinEvent(Channel channel, Chatter chatter) {
        super(channel, chatter);
    }

}