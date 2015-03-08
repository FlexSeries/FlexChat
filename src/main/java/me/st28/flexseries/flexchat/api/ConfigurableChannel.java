package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.*;

/**
 * Represents a {@link me.st28.flexseries.flexchat.api.Channel} that can be configured via the standard channel directory.
 */
public abstract class ConfigurableChannel extends Channel {

    private ChannelData data;

    void setData(ChannelData data) {
        this.data = data;
    }

    public ConfigurableChannel(String identifier) {
        super(identifier);
    }

    @Override
    public void save() {
        data.save();
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getShortName() {
        return data.getShortName() != null ? data.getShortName() : data.getName();
    }

    @Override
    public ChatColor getColor() {
        return data.getColor();
    }

    @Override
    public boolean hasOwnPermissions() {
        return data.getOption("permissions", true);
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
    public String getLogFormat() {
        return data.getLogFormat();
    }

    @Override
    public Collection<Chatter> getRecipients(Chatter sender) {
        Collection<Chatter> chatters = getChatters(sender);

        int chatRadius = data.getOption("chat radius", 0);

        if (!(sender instanceof PlayerChatter) || chatRadius <= 0) {
            return chatters;
        }

        if (chatRadius < 0) {
            data.getOptions().put("chat radius", 0);
            chatRadius = 0;
        }
        chatRadius = (int) Math.pow(chatRadius, 2D);

        Iterator<Chatter> iterator = chatters.iterator();

        Location location = ((PlayerChatter) sender).getPlayer().getLocation();
        while (iterator.hasNext()) {
            Chatter next = iterator.next();

            if (next instanceof PlayerChatter && ((PlayerChatter) next).getPlayer().getLocation().distanceSquared(location) > chatRadius) {
                iterator.remove();
            }
        }

        return chatters;
    }

    /**
     * @return the configuration data instance for this channel.
     */
    public final ChannelData getData() {
        return data;
    }

}