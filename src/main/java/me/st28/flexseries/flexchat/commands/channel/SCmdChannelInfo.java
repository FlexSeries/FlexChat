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
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.FlexSubcommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

//TODO: Implement
public final class SCmdChannelInfo extends FlexSubcommand<FlexChat> {

    public SCmdChannelInfo(FlexCommand<FlexChat> parent) {
        super(parent, "info", Collections.singletonList(new CommandArgument("channel", true)), new FlexCommandSettings()
                .description("View information about a channel")
                .permission(PermissionNodes.INFO)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        /*ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManagerImpl.class);
        Chatter chatter = chatterManager.getChatter(sender);

        boolean pageSpecified = false;
        int page = 1;

        Channel channel;

        if (args.length == 2) {
            page = CommandUtils.getPage(args, 1);
            channel = CmdChannel.matchChannel(args[0]);
            pageSpecified = true;
        } else if (args.length == 1) {
            page = CommandUtils.getPage(args, 0, true);
            if (page == -1) {
                channel = CmdChannel.matchChannel(args[0]);
                page = 1;
            } else {
                channel = chatter.getActiveChannel();
                pageSpecified = true;
            }
        } else {
            channel = chatter.getActiveChannel();
        }

        if (channel == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexChat.class, "errors.channel_none"));
        }

        String shortName = channel.getShortName();
        ChatColor color = channel.getColor();
        boolean isJoinableByCommand = channel.isJoinableByCommand();
        boolean isLeaveableByCommand = channel.isLeaveableByCommand();

        ListBuilder builder = new ListBuilder("page_subtitle", "Channel", color + channel.getName(), label);

        List<Chatter> recipients = new ArrayList<>(channel.getChatters(chatter));

        Iterator<Chatter> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            Chatter next = iterator.next();

            if (!next.isVisibleTo(chatter)) {
                iterator.remove();
            }
        }

        Collections.sort(recipients, new Comparator<Chatter>() {
            @Override
            public int compare(Chatter o1, Chatter o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        *//*List<String> banned;
        try {
            banned = new ArrayList<>(channel.getBanned());
            Collections.sort(banned, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.toLowerCase().compareTo(o2.toLowerCase());
                }
            });
        } catch (UnsupportedOperationException ex) {
            banned = null;
        }*//*

        builder.addMessage("title", "Short Name", shortName);
        builder.addMessage("title", "Color", color + color.name());
        builder.addMessage("title", "Joinable", ChatColorUtils.colorBoolean(isJoinableByCommand));
        builder.addMessage("title", "Leaveable", ChatColorUtils.colorBoolean(isLeaveableByCommand));
        //TODO: (none) if there are no chatters
        builder.addMessage("title", "Chatters", StringUtils.collectionToString(recipients, new StringConverter<Chatter>() {
            @Override
            public String toString(Chatter object) {
                return object.getDisplayName();
            }
        }, ", "));

        *//*if (banned != null) {
            builder.addMessage("title", "Banned", banned.isEmpty() ? ("" + ChatColor.RED + ChatColor.ITALIC + "(none)") : StringUtils.stringCollectionToString(banned, ", "));
        }*//*

        Map<String, String> customInfo = channel.getCustomData(chatter);
        if (customInfo != null) {
            for (Entry<String, String> entry : customInfo.entrySet()) {
                builder.addMessage("title", entry.getKey(), entry.getValue());
            }
        }

        builder.enableNextPageNotice(label + " " + StringUtils.stringCollectionToString(Arrays.asList(args).subList(0, pageSpecified ? args.length - 1 : args.length)));
        builder.sendTo(sender, page);*/
    }

}