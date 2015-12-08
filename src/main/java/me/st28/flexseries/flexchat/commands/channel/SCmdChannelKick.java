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
package me.st28.flexseries.flexchat.commands.channel;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.commands.arguments.ChannelArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChannelInstanceArgument;
import me.st28.flexseries.flexchat.commands.arguments.ChatterArgument;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.command.*;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.MessageReference;

public class SCmdChannelKick extends Subcommand<FlexChat> {

    public SCmdChannelKick(AbstractCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("kick").description("Kick a player from a channel").permission(PermissionNodes.KICK));

        addArgument(new ChatterArgument("player", true));
        addArgument(new ChannelArgument("channel", false));
        addArgument(new ChannelInstanceArgument("instance", false, "channel"));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Chatter target = context.getGlobalObject("player", Chatter.class);
        ChannelInstance instance = context.getGlobalObject("instance", ChannelInstance.class);
        Channel channel = instance.getChannel();

        boolean isActive = instance == target.getActiveInstance();

        if (instance.removeChatter(target)) {
            MessageReference message = MessageManager.getMessage(FlexChat.class, "alerts_channel.chatter_kicked",
                    new ReplacementMap("{CHATTER}", target.getName())
                            .put("{COLOR}", channel.getColor().toString())
                            .put("{CHANNEL}", channel.getName())
                            .put("{PLAYER}", context.getSender().getName())
                            .getMap()
            );

            instance.sendMessage(message);
            target.sendMessage(message);

            if (isActive && target.getActiveInstance() != null) {
                channel = target.getActiveInstance().getChannel();
                target.sendMessage(MessageManager.getMessage(FlexChat.class, "notices.channel_active_set",
                        new ReplacementMap("{COLOR}", channel.getColor().toString()).put("{CHANNEL}", channel.getName()).getMap()));
            }
        } else {
            throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.channel_not_joined_other",
                    new ReplacementMap("{PLAYER}", target.getName())
                            .put("{CHANNEL}", channel.getName())
                            .getMap())
            );
        }
    }

}