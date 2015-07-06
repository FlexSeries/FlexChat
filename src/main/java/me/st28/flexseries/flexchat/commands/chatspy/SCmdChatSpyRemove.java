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
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatadmin.ChatAdminManager;
import me.st28.flexseries.flexchat.backend.chatadmin.SpySettings;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.*;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;

public final class SCmdChatSpyRemove extends FlexSubcommand<FlexChat> {

    public SCmdChatSpyRemove(FlexCommand<FlexChat> parent) {
        super(parent, "remove", Arrays.asList(new CommandArgument("channel", true), new CommandArgument("instance", false)),
            new FlexCommandSettings<FlexChat>()
                .permission(PermissionNodes.SPY)
                .description("Remove a channel or channel instance from your spying list")
                .setPlayerOnly(true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);

        Channel channel = channelManager.getChannel(args[0]);
        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new ReplacementMap("{NAME}", args[0]).getMap()));
        }

        ChannelInstance instance = null;
        if (args.length > 1) {
            instance = channel.getInstance(args[1]);

            if (instance == null) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_instance_not_found", new ReplacementMap("{CHANNEL}", channel.getName()).put("{NAME}", args[1]).getMap()));
            }
        }

        SpySettings settings = FlexPlugin.getRegisteredModule(ChatAdminManager.class).getSpySettings(CommandUtils.getSenderUuid(sender));

        if (instance != null) {
            Map<String, String> map = new ReplacementMap("{INSTANCE}", instance.getDisplayName()).put("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeInstance(instance)) {
                MessageReference.create(FlexChat.class, "notices.spy_instance_removed", map).sendTo(sender);
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.spy_instance_already_removed", map));
            }
        } else {
            Map<String, String> map = new ReplacementMap("{CHANNEL}", channel.getName()).getMap();
            if (settings.removeChannel(channel)) {
                MessageReference.create(FlexChat.class, "notices.spy_channel_removed", map).sendTo(sender);
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.spy_channel_already_removed", map));
            }
        }
    }

}