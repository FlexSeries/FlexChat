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
package me.st28.flexseries.flexchat.commands.chatspy;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.DummyCommand;

public final class CmdChatSpy extends DummyCommand<FlexChat> {

    public CmdChatSpy(FlexChat plugin) {
        super(plugin, new CommandDescriptor("chatspy").permission(PermissionNodes.SPY));

        // TODO: Add list command
        registerSubcommand(new SCmdChatSpyAdd(this));
        registerSubcommand(new SCmdChatSpyDisable(this));
        registerSubcommand(new SCmdChatSpyEnable(this));
        registerSubcommand(new SCmdChatSpyRemove(this));
    }

}