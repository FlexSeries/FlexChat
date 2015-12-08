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
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.chatadmin.SpySettings;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChannelInstanceArgument;
import me.st28.flexseries.flexlib.command.*;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.entity.Player;

import java.util.Map;

public final class SCmdChatSpyRemove extends Subcommand<FlexChat> {

    public SCmdChatSpyRemove(FlexCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("remove").description("Removes a channel or channel instance from your spying list").playerOnly(true));

        addArgument(new ChannelArgument("channel", true));
        addArgument(new ChannelInstanceArgument("instance", false, "channel"));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Channel channel = context.getGlobalObject("channel", Channel.class);
        ChannelInstance instance = context.getGlobalObject("instance", ChannelInstance.class);

        SpySettings settings = FlexPlugin.getGlobalModule(ChatAdminManager.class).getSpySettings(((Player) context.getSender()).getUniqueId());

        if (!context.isDefaultValue("instance")) {
            Map<String, Object> map = new ReplacementMap("{INSTANCE}", instance.getDisplayName()).put("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeInstance(instance)) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.spy_instance_removed", map));
            } else {
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.spy_instance_already_removed", map));
            }
        } else {
            Map<String, Object> map = new ReplacementMap("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeChannel(channel)) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.spy_channel_removed", map));
            } else {
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.spy_channel_already_removed", map));
            }
        }
    }

}