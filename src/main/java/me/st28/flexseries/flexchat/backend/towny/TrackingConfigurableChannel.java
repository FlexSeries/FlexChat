package me.st28.flexseries.flexchat.backend.towny;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ConfigurableChannel;
import me.st28.flexseries.flexchat.api.PlayerChatter;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract  class TrackingConfigurableChannel extends ConfigurableChannel {

    private Set<Chatter> chatters = new HashSet<>();

    public TrackingConfigurableChannel(String identifier) {
        super(identifier);
    }

    @Override
    protected DynamicResponse addChatter(Chatter chatter, boolean silent) {
        DynamicResponse joinCheck = canChatterJoin(chatter);
        if (!joinCheck.isSuccess()) {
            return joinCheck;
        }

        if (!silent) {
            sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_joined", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", getName()).put("{COLOR}", getColor().toString()).getMap()));
        }

        chatters.add(chatter);
        return new DynamicResponse(true);
    }

    @Override
    protected DynamicResponse removeChatter(Chatter chatter, boolean silent) {
        if (!chatters.remove(chatter)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_not_joined", new QuickMap<>("{CHANNEL}", getName()).getMap()));
        }

        if (!silent) {
            sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_left", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", getName()).put("{COLOR}", getColor().toString()).getMap()));
        }
        return new DynamicResponse(true);
    }

    @Override
    protected void refreshChatters() {
        Iterator<Chatter> iterator = chatters.iterator();
        while (iterator.hasNext()) {
            Chatter next = iterator.next();

            if (next instanceof PlayerChatter && ((PlayerChatter) next).getPlayer() == null) {
                iterator.remove();
            }
        }
    }

}