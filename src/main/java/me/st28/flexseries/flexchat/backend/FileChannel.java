package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.api.BasicChannel;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * A channel that is loaded by FlexChat from files in the 'channels' directory.
 */
public class FileChannel extends BasicChannel {

    private final Map<String, String> formats = new HashMap<>();

    public FileChannel(String name, String shortName, ChatColor color, Collection<String> banned) {
        super(name, shortName, color, true, true, banned);

        this.formats.putAll(formats);
    }

    @Override
    public String getFormat(Chatter chatter) {
        if (chatter instanceof PlayerChatter) {
            try {
                String[] groups = FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getPermission().getPlayerGroups(null, ((PlayerChatter) chatter).getPlayer());

                List<String> playerGroups = new ArrayList<>();
                for (String group : groups) {
                    playerGroups.add(group.toLowerCase());
                }

                for (String group : formats.keySet()) {
                    if (playerGroups.contains(group)) {
                        return formats.get(group);
                    }
                }
            } catch (HookDisabledException ex) { }
        }

        return formats.get("default");
    }

    @Override
    public DynamicResponse canChatterJoin(Chatter chatter) {
        return new DynamicResponse(true, null);
    }

}