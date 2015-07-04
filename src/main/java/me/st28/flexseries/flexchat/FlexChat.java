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
package me.st28.flexseries.flexchat;

import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.ChatManager;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.commands.ignore.CmdIgnore;
import me.st28.flexseries.flexchat.commands.CmdMessage;
import me.st28.flexseries.flexchat.commands.CmdReply;
import me.st28.flexseries.flexchat.commands.channel.CmdChannel;
import me.st28.flexseries.flexchat.commands.chatspy.CmdChatSpy;
import me.st28.flexseries.flexchat.commands.ignore.CmdUnignore;
import me.st28.flexseries.flexcore.command.FlexCommandWrapper;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;

public final class FlexChat extends FlexPlugin {

    //public final static Logger CHAT_LOGGER = LogManager.getLogger(ChannelManager.class.getCanonicalName());

    @Override
    public void handlePluginLoad() {
        registerModule(new ChannelManagerImpl(this));
        registerModule(new ChatterManagerImpl(this));
        registerModule(new ChatManager(this));
        registerModule(new ChatAdminManager(this));
    }

    @Override
    public void handlePluginEnable() {
        FlexCommandWrapper.registerCommand(this, new CmdChannel(this));
        FlexCommandWrapper.registerCommand(this, new CmdChatSpy(this));
        FlexCommandWrapper.registerCommand(this, new CmdIgnore(this));
        FlexCommandWrapper.registerCommand(this, new CmdUnignore(this));

        CmdMessage messageCommand = new CmdMessage(this);
        FlexCommandWrapper.registerCommand(this, messageCommand);
        FlexCommandWrapper.registerCommand(this, new CmdReply(this, messageCommand));
    }

}