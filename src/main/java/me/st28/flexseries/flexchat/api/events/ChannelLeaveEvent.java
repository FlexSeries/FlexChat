package me.st28.flexseries.flexchat.api.events;

import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.Chatter;

/**
 * Fired when a chatter leaves a channel.
 */
public final class ChannelLeaveEvent extends ChannelEvent {

    public ChannelLeaveEvent(Channel channel, Chatter chatter) {
        super(channel, chatter);
    }

}