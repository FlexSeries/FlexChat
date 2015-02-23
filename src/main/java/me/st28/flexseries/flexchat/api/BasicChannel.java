package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexchat.backend.PlayerChatter;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.*;

public abstract class BasicChannel extends Channel {

    private String shortName;
    private ChatColor color;
    private int chatRadius = -1;

    private final Map<String, String> formats = new HashMap<>();

    private final Set<String> banned = new HashSet<>();

    public BasicChannel(String name, boolean isJoinableByCommand, boolean isLeaveableByCommand, String shortName, ChatColor color, int chatRadius, Collection<String> banned, Map<String, String> formats) {
        super(name, isJoinableByCommand, isLeaveableByCommand);

        this.shortName = shortName;
        this.color = color;
        this.chatRadius = chatRadius;

        this.formats.putAll(formats);

        if (banned != null) {
            this.banned.addAll(banned);
        }
    }

    @Override
    public ChatColor getColor() {
        return color;
    }

    @Override
    public String getShortName() {
        return shortName == null ? super.getShortName() : shortName;
    }

    public int getChatRadius() {
        return chatRadius;
    }

    @Override
    public boolean isVisibleTo(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<Chatter> getRecipients(Chatter target) {
        if (chatRadius == -1 || !(target instanceof PlayerChatter)) {
            return getChatters(target);
        }

        List<Chatter> returnList = new ArrayList<>();
        double radiusSquared = Math.pow(chatRadius, 2D);
        Location senderLoc = ((PlayerChatter) target).getPlayer().getLocation();

        for (Chatter chatter : getChatters(target)) {
            try {
                if (chatter instanceof PlayerChatter && senderLoc.distanceSquared(((PlayerChatter) chatter).getPlayer().getLocation()) > radiusSquared) {
                    continue;
                }
            } catch (IllegalArgumentException ex) {
                continue;
            }
            returnList.add(chatter);
        }

        return returnList;
    }

    @Override
    public Collection<Chatter> getChatters(Chatter target) {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);

        List<Chatter> chatters = new ArrayList<>();

        for (Chatter chatter : channelManager.getChatters()) {
            if (chatter.getChannels().contains(this)) {
                chatters.add(chatter);
            }
        }

        return chatters;
    }

    @Override
    public Collection<String> getBanned() {
        return Collections.unmodifiableSet(banned);
    }

}