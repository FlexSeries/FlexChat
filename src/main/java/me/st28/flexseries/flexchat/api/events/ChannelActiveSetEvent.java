package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;

/**
 * Fired when a chatter sets their active channel.
 */
public final class ChannelActiveSetEvent extends ChannelEvent {

    public ChannelActiveSetEvent(Channel channel, Chatter chatter) {
        super(channel, chatter);
    }

}