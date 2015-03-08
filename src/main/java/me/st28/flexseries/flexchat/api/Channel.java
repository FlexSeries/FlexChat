package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a realm in which chatters can chat.
 */
public abstract class Channel {

    private final String identifier;

    public Channel(String identifier) {
        Validate.notNull(identifier, "Identifier cannot be null.");

        this.identifier = identifier;
    }

    /**
     * Saves data that needs to be saved for the channel.
     */
    public void save() { }

    /**
     * @return the unique identifier for the channel.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return the display name of the channel.
     */
    public abstract String getName();

    /**
     * @return the short name of the channel.
     */
    public abstract String getShortName();

    /**
     * @return the default ChatColor for this channel.
     */
    public abstract ChatColor getColor();

    /**
     * @return true to signify that this channel should not use FlexChat's default permissions.
     */
    public abstract boolean hasOwnPermissions();

    /**
     * @return false if the channel cannot be joined with the default channel join command.
     */
    public boolean isJoinableByCommand() {
        return true;
    }

    /**
     * @return false if the channel cannot be left with the default channel leave command.
     */
    public boolean isLeaveableByCommand() {
        return true;
    }

    /**
     * @return false if the channel shouldn't be visible to the specified chatter on the channel list command.
     */
    public boolean isVisibleTo(Chatter chatter) {
        return true;
    }

    /**
     * Checks whether or not a chatter can join the channel.<br />
     * This method is essentially {@link #addChatter(Chatter, boolean)} without actually adding the chatter or changing anything.
     *
     * @return a {@link me.st28.flexseries.flexcore.utils.DynamicResponse} representing whether or not a chatter can join.<br />
     *         By default, channels are unjoinable.
     */
    public DynamicResponse canChatterJoin(Chatter chatter) {
        return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_join_unable", new QuickMap<>("{CHANNEL}", getName()).getMap()));
    }

    /**
     * Adds a chatter to the channel.
     *
     * @return A {@link me.st28.flexseries.flexcore.utils.DynamicResponse} representing the output of this method.
     */
    protected abstract DynamicResponse addChatter(Chatter chatter, boolean silent);

    /**
     * Removes a chatter from the channel.
     *
     * @return A {@link me.st28.flexseries.flexcore.utils.DynamicResponse} representing the output of this method.
     */
    protected abstract DynamicResponse removeChatter(Chatter chatter, boolean silent);

    protected void refreshChatters() { }

    /**
     * @return the chat format for a specified sender.
     */
    public abstract String getChatFormat(Chatter sender);

    /**
     * @return the chat format to use for logging purposes.
     */
    public abstract String getLogFormat();

    /**
     * @return a read-only collection of chatters that exist in the sender's instance of the channel.
     */
    public abstract Collection<Chatter> getChatters(Chatter sender);

    /**
     * @return a read-only collection of chatters that exist in the sender's instance of the channel
     *         <i>AND</i> are currently able to receive a message from the sender.
     */
    public abstract Collection<Chatter> getRecipients(Chatter sender);

    /**
     * @return a map of custom data to display on the channel information command for this channel.
     */
    public Map<String, String> getCustomData(Chatter recipient) {
        return new LinkedHashMap<>();
    }

    public void sendMessage(Chatter sender, MessageReference message) {
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(message, "Message cannot be null.");

        for (Chatter chatter : getChatters(sender)) {
            chatter.sendMessage(message);
        }
    }

}