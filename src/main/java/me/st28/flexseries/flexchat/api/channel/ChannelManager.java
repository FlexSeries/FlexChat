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

import me.st28.flexseries.flexchat.api.format.ChatFormat;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents FlexChat's channel handler.
 */
public interface ChannelManager {

    /**
     * @return A global format with the given group.
     */
    ChatFormat getGlobalFormat(String group);

    /**
     * Registers a {@link Channel}.
     *
     * @return True if the channel was successfully registered.<br />
     *         False if another channel with the same name is already registered.
     */
    boolean registerChannel(Channel channel);

    /**
     * Registers a {@link Channel} with a default configuration file.
     *
     * @see #registerChannel(Channel)
     */
    boolean registerChannel(Channel channel, InputStream defaultConfig);

    /**
     * Unregisters a {@link Channel}.
     *
     * @return True if the channel was successfully unregistered.<br />
     *         False if the channel isn't registered under the manager.
     */
    boolean unregisterChannel(Channel channel);

    /**
     * @return The default {@link Channel}.
     */
    Channel getDefaultChannel();

    /**
     * @return An unmodifiable collection of all loaded {@link Channel}s.
     */
    Collection<Channel> getChannels();

    /**
     * Retrieves a {@link Channel} based on its name.
     *
     * @return A registered channel matching the given name.<br />
     *         Null if there are no matched channels.
     */
    Channel getChannel(String name);

    /**
     * @return True if the specified channel is muted.<br />
     *         False otherwise.
     */
    boolean isChannelMuted(Channel channel);

    /**
     * @return The number of seconds left for the channel's mute time.<br />
     *         < 0 for an indefinite amount of time or a non-muted channel.
     */
    int getChannelMuteTime(Channel channel);

    /**
     * Mutes a {@link Channel}.
     *
     * @param channel The channel to mute.
     * @param seconds The number of seconds to mute the channel for.<br />
     *                < 0 for an indefinite amount of time.
     * @return True if the channel was muted successfully.<br />
     *         False if the channel is already muted.
     */
    boolean muteChannel(Channel channel, int seconds);

    /**
     * Unmutes a {@link Channel}.
     *
     * @param channel The channel to unmute.
     * @return True if the channel was unmuted successfully.<br />
     *         False if the channel is already unmuted.
     */
    boolean unmuteChannel(Channel channel);

}