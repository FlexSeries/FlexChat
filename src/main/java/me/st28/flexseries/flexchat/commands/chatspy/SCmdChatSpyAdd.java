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

public final class SCmdChatSpyAdd extends Subcommand<FlexChat> {

    public SCmdChatSpyAdd(FlexCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("add").description("Add a channel or channel instance to your spying list").playerOnly(true));

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
            if (settings.addInstance(instance)) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.spy_instance_added", map));
            } else {
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.spy_instance_already_added", map));
            }
        } else {
            Map<String, Object> map = new ReplacementMap("{CHANNEL}", channel.getName()).getMap();
            if (settings.addChannel(channel)) {
                throw new CommandInterruptedException(InterruptReason.COMMAND_END, MessageManager.getMessage(FlexChat.class, "notices.spy_channel_added", map));
            } else {
                throw new CommandInterruptedException(InterruptReason.COMMAND_SOFT_ERROR, MessageManager.getMessage(FlexChat.class, "errors.spy_channel_already_added", map));
            }
        }
    }

}