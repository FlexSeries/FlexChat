package me.st28.flexseries.flexchat.commands;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.ChatColorUtils;
import me.st28.flexseries.flexcore.utils.StringConverter;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.Map.Entry;

public final class SCmdChannelInfo extends FlexCommand<FlexChat> {

    public SCmdChannelInfo(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"info"},
                parent,
                new FlexCommandSettings<FlexChat>()
                        .description("View information about a channel"),
                new CommandArgument("channel", false),
                new CommandArgument("page", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);
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
        Collections.sort(recipients, new Comparator<Chatter>() {
            @Override
            public int compare(Chatter o1, Chatter o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        /*List<String> banned;
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
        }*/

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

        /*if (banned != null) {
            builder.addMessage("title", "Banned", banned.isEmpty() ? ("" + ChatColor.RED + ChatColor.ITALIC + "(none)") : StringUtils.stringCollectionToString(banned, ", "));
        }*/

        Map<String, String> customInfo = channel.getCustomData(chatter);
        if (customInfo != null) {
            for (Entry<String, String> entry : customInfo.entrySet()) {
                builder.addMessage("title", entry.getKey(), entry.getValue());
            }
        }

        builder.enableNextPageNotice(label + " " + StringUtils.stringCollectionToString(Arrays.asList(args).subList(0, pageSpecified ? args.length - 1 : args.length)));
        builder.sendTo(sender, page);
    }

}