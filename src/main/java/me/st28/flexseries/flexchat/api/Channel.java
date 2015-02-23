package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Channel {

    private final String name;

    private boolean isJoinableByCommand;
    private boolean isLeaveableByCommand;

    public Channel(String name, boolean isJoinableByCommand, boolean isLeaveableByCommand) {
        Validate.notNull(name, "Name cannot be null.");
        Validate.isTrue(ChannelManager.CHANNEL_NAME_PATTERN.matcher(name).matches(), "Name cannot contain spaces or be purely numbers.");
        this.name = name;

        this.isJoinableByCommand = isJoinableByCommand;
        this.isLeaveableByCommand = isLeaveableByCommand;
    }

    public final String getName() {
        return name;
    }

    /**
     * @return true if this channel can be joined via FlexChat's join command.
     */
    public final boolean isJoinableByCommand() {
        return isJoinableByCommand;
    }

    /**
     * @return true if this channel can be left via FlexChat's leave command.
     */
    public final boolean isLeaveableByCommand() {
        return isLeaveableByCommand;
    }

    /**
     * Sends a message to the channel manually.
     */
    public final void sendMessage(Chatter sender, String message) {
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(message, "Message cannot be null.");

        for (Chatter chatter : getChatters(sender)) {
            chatter.sendMessage(message);
        }
    }

    /**
     * Sends a message to the channel manually.
     */
    public final void sendMessage(Chatter sender, MessageReference message) {
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(message, "Message cannot be null.");

        for (Chatter chatter : getChatters(sender)) {
            chatter.sendMessage(message);
        }
    }

    /**
     * @return the main color for the channel.
     */
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    /**
     * @return the short name of the channel.
     */
    public String getShortName() {
        return getName();
    }

    /**
     * @return custom information that should appear on the channel info command for a specified sender.
     */
    public Map<String, String> getCustomInfo(CommandSender sender) {
        return new LinkedHashMap<>();
    }

    /**
     * Checks whether or not a chatter can join the channel.<br />
     * FlexChat itself will check if the chatter is already in the channel, so there's no need to send an error message here, if they are.
     *
     * @return a {@link me.st28.flexseries.flexcore.utils.DynamicResponse} representing whether or not a chatter can join.<br />
     *         By default, channels are unjoinable.
     */
    public DynamicResponse canChatterJoin(Chatter chatter) {
        return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_join_unable", new QuickMap<>("{CHANNEL}", name).getMap()));
    }

    /**
     * @return a collection of other chatters that exist in another chatter's instance  of the channel and are able to
     *         receive a message from a specified member of the channel.
     */
    public Collection<Chatter> getRecipients(Chatter target) {
        Validate.notNull(target, "Target cannot be null.");
        return getChatters(target);
    }

    /**
     * @return true if this channel should show on the channel list command for a specific sender.
     */
    public boolean isVisibleTo(CommandSender sender) {
        return true;
    }

    /**
     * @return returns the format that should be used for a particular chatter.
     */
    public abstract String getFormat(Chatter chatter);

    /**
     * @return a collection of other chatters that exist in another chatter's instance of the channel.
     */
    public abstract Collection<Chatter> getChatters(Chatter target);

    /**
     * @return a collection of chatter identifiers that are banned from this channel.
     */
    public Collection<String> getBanned() {
        throw new UnsupportedOperationException("Bans aren't supported by this channel.");
    }

    /**
     * @return true if the chatter was successfully banned.
     */
    public boolean banChatter(Chatter chatter) {
        throw new UnsupportedOperationException("Bans aren't supported by this channel.");
    }

}