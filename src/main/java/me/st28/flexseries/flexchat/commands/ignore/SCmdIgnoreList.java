package me.st28.flexseries.flexchat.commands.ignore;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.players.PlayerUUIDTracker;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class SCmdIgnoreList extends FlexCommand<FlexChat> {

    public SCmdIgnoreList(FlexChat plugin, FlexCommand<FlexChat> parent) {
        super(
                plugin,
                new String[]{"list"},
                parent,
                null
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);

        Chatter senderChatter = chatterManager.getChatter(sender);

        PlayerUUIDTracker uuidTracker = FlexPlugin.getRegisteredModule(PlayerUUIDTracker.class);

        List<String> names = new ArrayList<>();
        for (String identifier : senderChatter.getIgnored()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(identifier);
            } catch (Exception ex) {
                names.add(identifier);
                continue;
            }

            names.add(uuidTracker.getName(uuid));
        }

        Collections.sort(names);

        String namesFormatted = StringUtils.stringCollectionToString(names, ChatColor.GRAY + ", ");

        ListBuilder builder = new ListBuilder("title", "Ignored", null, label);
        if (!names.isEmpty()) {
            builder.addMessage(namesFormatted);
        }
        builder.sendTo(sender, 1);
    }

}