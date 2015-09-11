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