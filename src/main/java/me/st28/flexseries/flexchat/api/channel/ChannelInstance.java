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

import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import org.apache.commons.lang.Validate;

import java.util.*;

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

    public Collection<Chatter> getChatters() {
        return Collections.unmodifiableCollection(chatters);
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

    public void removeAllChatters() {
        Iterator<Chatter> iterator = chatters.iterator();
        while (iterator.hasNext()) {
            removeChatter(iterator.next());
        }
    }

    public void sendMessage(MessageReference message) {
        for (Chatter chatter : chatters) {
            chatter.sendMessage(message);
        }
    }

}