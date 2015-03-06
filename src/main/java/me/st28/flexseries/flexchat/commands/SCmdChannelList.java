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
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class SCmdChannelList extends FlexCommand<FlexChat> {

    public SCmdChannelList(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"list"},
                parent,
                new FlexCommandSettings<FlexChat>()
                        .description("Lists channels"),
                new CommandArgument("page", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        int page = CommandUtils.getPage(args, 0, false);

        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);

        Chatter chatter = chatterManager.getChatter(sender);

        final List<Channel> channels = new ArrayList<>(channelManager.getChannels());
        final Channel activeChannel = chatter.getActiveChannel();

        /*
         * SORT ORDER:
         * 1) Active channel
         * 2) Joined channels (alphabetical)
         * 3) Unjoined channels (alphabetical)
         */
        Collections.sort(channels, new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                if (o1 == o2) return 0;

                if (o2 == activeChannel) {
                    // If the other channel is active, it is automatically first.
                    return 1;
                } else if (o1 == activeChannel) {
                    // If the first channel is active, it is automatically first.
                    return -1;
                }

                boolean isJoin1 = channels.contains(o1);
                boolean isJoin2 = channels.contains(o2);

                if (isJoin1 != isJoin2) {
                    // If only one of the channels is joined, the joined one is first.
                    return isJoin1 ? 1 : -1;
                } else {
                    // If both channels are joined or unjoined, they are sorted alphabetically.
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            }
        });

        // Remove channels that shouldn't be visible on the list command.
        Iterator<Channel> it = channels.iterator();
        while (it.hasNext()) {
            if (!it.next().isVisibleTo(chatter)) it.remove();
        }

        ListBuilder builder = new ListBuilder("page", "Channels", null, label);

        Collection<Channel> chatterChannels = chatter.getChannels();
        String identifier = chatter.getIdentifier();
        for (Channel channel : channels) {
            String status;
            if (chatterChannels.contains(channel)) {
                status = ChatColor.GREEN + "joined";
            /*} else if (channel.getBanned().contains(identifier)) {
                status = "" + ChatColor.DARK_RED + ChatColor.ITALIC + "banned";*/
            } else {
                status = ChatColor.RED + "not joined";
            }

            int chatters = channel.getChatters(chatter).size();
            builder.addMessage("chat_channel", new QuickMap<>("{CHANNEL}", channel.getName())
                    .put("{COLOR}", channel.getColor().toString())
                    .put("{STATUS}", status)
                    .put("{CHATTERS}", Integer.toString(chatters))
                    .put("{S}", chatters == 1 ? "" : "s")
                    .put("{ACTIVE}", channel == chatter.getActiveChannel() ? channelManager.getActiveSymbol() : "")
                    .getMap()
            );
        }

        builder.sendTo(sender, page);
    }

}