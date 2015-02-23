package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;

/**
 * Represents a replaceable part of a chat format.
 */
public abstract class ChatVariable {

    public static void registerVariable(ChatVariable variable) {
        FlexPlugin.getRegisteredModule(ChannelManager.class).registerChatVariable(variable);
    }

    private String key;

    public ChatVariable(String key) {
        this.key = key;
    }

    public String getKey() {
        return "{" + key + "}";
    }

    public String getRawKey() {
        return key;
    }

    /**
     * Returns the replacement for a specified chatter.
     *
     * @param chatter The chatter that the replacement is being fired for.
     * @return The text to use.<br />
     *         Null to hide the variable.
     */
    public abstract String getReplacement(Chatter chatter, Channel channel);

}