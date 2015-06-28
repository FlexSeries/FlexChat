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

import me.st28.flexseries.flexchat.api.format.ChatFormat;

/**
 * Represents FlexChat's channel handler.
 */
public interface ChannelManager {

    /**
     * @return A global format with the given group.
     */
    ChatFormat getGlobalFormat(String group);

    /**
     * Registers a channel.
     *
     * @return True if the channel was successfully registered.<br />
     *         False if another channel with the same name is already registered.
     */
    boolean registerChannel(Channel channel);

    /**
     * Unregisters a channel.
     *
     * @return True if the channel was successfully unregistered.<br />
     *         False if the channel isn't registered under the manager.
     */
    boolean unregisterChannel(Channel channel);

    /**
     * Retrieves a {@link Channel} based on its name.
     *
     * @return A registered channel matching the given name.<br />
     *         Null if there are no matched channels.
     */
    Channel getChannel(String name);

}