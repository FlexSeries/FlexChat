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
package me.st28.flexseries.flexchat;

import me.st28.flexseries.flexchat.backend.ChatManager;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.backend.format.FormatManager;
import me.st28.flexseries.flexchat.commands.CmdMessage;
import me.st28.flexseries.flexchat.commands.CmdReply;
import me.st28.flexseries.flexchat.commands.channel.CmdChannel;
import me.st28.flexseries.flexchat.commands.chatspy.CmdChatSpy;
import me.st28.flexseries.flexchat.commands.ignore.CmdIgnore;
import me.st28.flexseries.flexchat.commands.ignore.CmdUnignore;
import me.st28.flexseries.flexchat.logging.ChatLogHelper;
import me.st28.flexseries.flexlib.command.FlexCommandWrapper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class FlexChat extends FlexPlugin {

    @Override
    public void handleLoad() {
        registerModule(new ChannelManagerImpl(this));
        registerModule(new ChatterManagerImpl(this));
        registerModule(new ChatManager(this));
        registerModule(new ChatAdminManager(this));
        registerModule(new FormatManager(this));
    }

    @Override
    public void handleEnable() {
        // Setup commands
        FlexCommandWrapper.registerCommand(new CmdChannel(this));
        FlexCommandWrapper.registerCommand(new CmdChatSpy(this));
        FlexCommandWrapper.registerCommand(new CmdIgnore(this));
        FlexCommandWrapper.registerCommand(new CmdUnignore(this));

        CmdMessage messageCommand = new CmdMessage(this);
        FlexCommandWrapper.registerCommand(messageCommand);
        FlexCommandWrapper.registerCommand(new CmdReply(this, messageCommand));

        // Setup logger
        ChatLogHelper.init(this);
    }

    @Override
    public void handleConfigReload(FileConfiguration config) {
        ChatLogHelper.reload(config);
    }

}