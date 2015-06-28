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
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.FlexSubcommand;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

//TODO: Leave specified channel
public class SCmdChannelLeave extends FlexSubcommand<FlexChat> {

    public SCmdChannelLeave(FlexCommand<FlexChat> parent) {
        super(
                parent,
                "leave",
                Collections.singletonList(new CommandArgument("channel", false)),
                new FlexCommandSettings<FlexChat>()
                        .description("Leaves currently active channel or a specified channel")
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChannelManagerImpl channelManager = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class);
        ChatterManagerImpl chatterManager = FlexPlugin.getRegisteredModule(ChatterManagerImpl.class);
        Chatter chatter = chatterManager.getChatter(sender);

        Channel channel = null;
        if (args.length > 0) {
            channel = channelManager.getChannel(args[0]);
            if (channel == null) {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_found", new ReplacementMap("{NAME}", args[0]).getMap()));
            }
        } else {
            ChannelInstance activeInstance = chatter.getActiveInstance();
            if (activeInstance != null) {
                channel = activeInstance.getChannel();
            }
        }

        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_active_not_set"));
        }

        if (!chatter.hasPermission(PermissionNodes.buildVariableNode(PermissionNodes.LEAVE, channel.getName()))) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "leave").put("{CHANNEL}", channel.getName()).getMap()));
        }

        List<ChannelInstance> instances = channel.getInstances(chatter);

        if (instances.size() == 1) {
            if (chatter.removeInstance(instances.get(0))) {
                MessageReference message = MessageReference.create(FlexChat.class, "alerts_channel.chatter_left", new ReplacementMap("{CHATTER}", chatter.getName()).put("{COLOR}", channel.getColor().toString()).put("{CHANNEL}", channel.getName()).getMap());
                instances.get(0).sendMessage(message);
                chatter.sendMessage(message);
                //TODO: Set next active channel
            } else {
                throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_not_joined", new ReplacementMap("{CHANNEL}", channel.getName()).getMap()));
            }
        } else {
            //TODO: Implement leaving different instance
            sender.sendMessage("NOT IMPLEMENTED");
        }
    }

}