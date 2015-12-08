/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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