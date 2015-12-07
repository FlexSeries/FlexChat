/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexchat.api.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an instance of a channel.
 */
public class ChannelInstance {

    private Channel channel;

    private String label;

    private final Set<Chatter> chatters = new HashSet<>();

    public ChannelInstance(Channel channel, String label) {
        Validate.notNull(channel, "Channel cannot be null.");

        this.channel = channel;
        this.label = label;
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * @return A human readable label for easy identification.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return A separate, easier to identify name to display instead of the label.
     */
    public String getDisplayName() {
        return label;
    }

    /**
     * @return An unmodifiable collection of all chatters that have joined the instance.
     */
    public Collection<Chatter> getChatters() {
        return Collections.unmodifiableCollection(chatters);
    }

    /**
     * @return A collection of all chatters in the instance that can receive a message sent by the
     *         provided chatter.
     */
    public Collection<Chatter> getApplicableChatters(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");

        int range = channel.getRadius();
        if (range == 0 || !(chatter instanceof ChatterPlayer)) {
            return new HashSet<>(chatters);
        }

        Set<Chatter> returnSet = new HashSet<>();

        Location senderLoc = ((ChatterPlayer) chatter).getPlayer().getLocation();
        double radius = Math.pow(channel.getRadius(), 2D);

        for (Chatter oChatter : chatters) {
            if (oChatter == null) {
                continue;
            }

            if (oChatter instanceof ChatterPlayer) {
                try {
                    Player oPlayer = ((ChatterPlayer) oChatter).getPlayer();
                    if (oPlayer != null && oPlayer.getLocation().distanceSquared(senderLoc) > radius) {
                        continue;
                    }
                } catch (IllegalArgumentException ex) {
                    // Different worlds.
                    continue;
                }
            }

            returnSet.add(oChatter);
        }

        return returnSet;
    }

    public boolean containsChatter(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");
        return chatters.contains(chatter);
    }

    public boolean addChatter(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");

        boolean result = chatters.add(chatter);

        if (!chatter.isInInstance(this)) {
            chatter.addInstance(this);
        }

        return result;
    }

    public boolean removeChatter(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");

        boolean result = chatters.remove(chatter);

        if (chatter.isInInstance(this)) {
            chatter.removeInstance(this);
        }

        return result;
    }

    /**
     * Removes a chatter from this instance, BUT NOT THE CHANNEL FROM THE CHATTER.
     * This should ONLY be used when the chatter is being unloaded.
     */
    public void removeOfflineChatter(Chatter chatter) {
        chatters.remove(chatter);
    }

    public void removeAllChatters() {
        Set<Chatter> toRemove = new HashSet<>(chatters);

        for (Chatter chatter : toRemove) {
            removeChatter(chatter);
        }
    }

    public void sendMessage(MessageReference message) {
        for (Chatter chatter : chatters) {
            chatter.sendMessage(message);
        }
    }

    public void alertJoin(Chatter chatter) {
        List<Chatter> single = new ArrayList<>();
        List<Chatter> multiple = new ArrayList<>();

        for (Chatter oChatter : chatters) {
            if (oChatter.getInstanceCount(channel) > 1) {
                multiple.add(oChatter);
            } else {
                single.add(oChatter);
            }
        }

        MessageReference message = MessageManager.getMessage(FlexChat.class, "alerts_channel.chatter_joined",
                new ReplacementMap("{CHATTER}", chatter.getDisplayName())
                        .put("{CHANNEL}", channel.getName())
                        .put("{COLOR}", channel.getColor().toString())
                        .getMap());

        // Send to chatters that only are in this instance for the channel
        for (Chatter oChatter : single) {
            oChatter.sendMessage(message);
        }

        // Handles chatters that are in multiple instances of this channel
        if (!multiple.isEmpty()) {
            if (getDisplayName() == null) {
                for (Chatter oChatter : multiple) {
                    oChatter.sendMessage(message);
                }
                return;
            }

            // Display name is not null, can specify instance name
            MessageReference specificMessage = MessageManager.getMessage(FlexChat.class, "alerts_channel.chatter_joined_specific",
                    new ReplacementMap("{CHATTER}", chatter.getDisplayName())
                            .put("{CHANNEL}", channel.getName())
                            .put("{COLOR}", channel.getColor().toString())
                            .put("{INSTANCE}", getDisplayName())
                            .getMap());

            for (Chatter oChatter : multiple) {
                oChatter.sendMessage(specificMessage);
            }
        }
    }

    public void alertLeave(Chatter chatter) {
        List<Chatter> single = new ArrayList<>();
        List<Chatter> multiple = new ArrayList<>();

        Set<Chatter> newChatters = new HashSet<>(chatters);
        newChatters.add(chatter);

        for (Chatter oChatter : newChatters) {
            if (oChatter.getInstanceCount(channel) > 1) {
                multiple.add(oChatter);
            } else {
                single.add(oChatter);
            }
        }

        MessageReference message = MessageManager.getMessage(FlexChat.class, "alerts_channel.chatter_left",
                new ReplacementMap("{CHATTER}", chatter.getDisplayName())
                        .put("{CHANNEL}", channel.getName())
                        .put("{COLOR}", channel.getColor().toString())
                        .getMap());

        // Send to chatters that only are in this instance for the channel
        for (Chatter oChatter : single) {
            oChatter.sendMessage(message);
        }

        // Handles chatters that are in multiple instances of this channel
        if (!multiple.isEmpty()) {
            if (getDisplayName() == null) {
                for (Chatter oChatter : multiple) {
                    oChatter.sendMessage(message);
                }
                return;
            }

            // Display name is not null, can specify instance name
            MessageReference specificMessage = MessageManager.getMessage(FlexChat.class, "alerts_channel.chatter_left_specific",
                    new ReplacementMap("{CHATTER}", chatter.getDisplayName())
                            .put("{CHANNEL}", channel.getName())
                            .put("{COLOR}", channel.getColor().toString())
                            .put("{INSTANCE}", getDisplayName())
                            .getMap());

            for (Chatter oChatter : multiple) {
                oChatter.sendMessage(specificMessage);
            }
        }
    }

}