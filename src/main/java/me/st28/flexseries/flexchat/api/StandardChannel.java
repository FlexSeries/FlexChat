package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.ChatColor;

import java.util.*;

public class StandardChannel extends ConfigurableChannel {

    private final Set<Chatter> chatters = new HashSet<>();

    public StandardChannel(String identifier) {
        super(identifier);
    }

    @Override
    public DynamicResponse canChatterJoin(Chatter chatter) {
        if (chatters.contains(chatter)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_already_joined", new QuickMap<>("{CHANNEL}", getName()).getMap()));
        }
        return new DynamicResponse(true);
    }

    @Override
    public DynamicResponse addChatter(Chatter chatter, boolean silent) {
        DynamicResponse joinCheck = canChatterJoin(chatter);
        if (!joinCheck.isSuccess()) {
            return joinCheck;
        }

        if (!silent) {
            sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_joined", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", getName()).put("{COLOR}", getColor().toString()).getMap()));
        }

        chatters.add(chatter);
        return new DynamicResponse(true);
    }

    @Override
    public DynamicResponse removeChatter(Chatter chatter, boolean silent) {
        if (!chatters.remove(chatter)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_not_joined", new QuickMap<>("{CHANNEL}", getName()).getMap()));
        }

        if (!silent) {
            sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_left", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", getName()).put("{COLOR}", getColor().toString()).getMap()));
        }
        return new DynamicResponse(true);
    }

    @Override
    public String getChatFormat(Chatter sender) {
        if (sender instanceof PlayerChatter) {
            try {
                String[] groups = FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getPermission().getPlayerGroups(null, ((PlayerChatter) sender).getPlayer());

                List<String> playerGroups = new ArrayList<>();
                for (String group : groups) {
                    playerGroups.add(group.toLowerCase());
                }

                Map<String, String> formats = data.getFormats();
                for (String group : formats.keySet()) {
                    if (playerGroups.contains(group)) {
                        return formats.get(group);
                    }
                }
            } catch (HookDisabledException | ModuleDisabledException ex) { }
        }

        return data.getFormat("default");
    }

    @Override
    public Collection<Chatter> getChatters(Chatter sender) {
        return chatters;
    }

    @Override
    public Collection<Chatter> getRecipients(Chatter sender) {
        return chatters;
    }

    @Override
    public Map<String, String> getCustomData(Chatter recipient) {
        Map<String, String> customData = super.getCustomData(recipient);

        Integer chatRadius = data.getOption("chat radius", null);
        if (chatRadius != null) {
            customData.put("Chat Radius", chatRadius <= 0 ? ("" + ChatColor.GREEN + ChatColor.ITALIC + "global") : Integer.toString(chatRadius));
        }

        return customData;
    }

}