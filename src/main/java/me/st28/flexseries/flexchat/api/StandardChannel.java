package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class StandardChannel extends ConfigurableChannel {

    private final Set<Chatter> chatters = new HashSet<>();

    public StandardChannel(String identifier) {
        super(identifier);
    }

    @Override
    public DynamicResponse canChatterJoin(Chatter chatter) {
        if (chatters.contains(chatter)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_already_joined", new QuickMap<>("{CHANNEL}", getName()).getMap()));
        }

        DynamicResponse superResponse = super.canChatterJoin(chatter);
        if (!superResponse.isSuccess()) {
            return superResponse;
        }
        return new DynamicResponse(true);
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
    public Collection<Chatter> getChatters(Chatter sender) {
        return chatters;
    }

}