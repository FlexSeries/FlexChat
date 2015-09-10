package me.st28.flexseries.flexchat.backend.format;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterConsole;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class FormatManager extends FlexModule<FlexChat> {

    private final Map<String, String> messageFormats = new HashMap<>();

    public FormatManager(FlexChat plugin) {
        super(plugin, "formats", "Handles chat formats", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleReload() {
        final ConfigurationSection config = getConfig();

        messageFormats.clear();
        final ConfigurationSection messageSec = config.getConfigurationSection("message formats");
        if (messageSec != null) {
            for (String group : messageSec.getKeys(false)) {
                messageFormats.put(group.toLowerCase(), messageSec.getString(group));
            }
        }

        if (!messageFormats.containsKey("default")) {
            messageFormats.put("default", "&f[&7{SENDER} &f\\u27A1 &7{RECEIVER}&f] &7{MESSAGE}");
        }
    }

    public String getFormat(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");

        if (chatter instanceof ChatterConsole) {
            return messageFormats.containsKey("console") ? messageFormats.get("console") : messageFormats.get("default");
        }

        VaultHook vault;
        try {
            vault = FlexPlugin.getGlobalModule(HookManager.class).getHook(VaultHook.class);
        } catch (Exception ex) {
            // Vault not loaded successfully.
            return messageFormats.get("default");
        }

        List<String> playerGroups = Arrays.asList(vault.getPermission().getPlayerGroups(null, ((ChatterPlayer) chatter).getPlayer())).stream().map(String::toLowerCase).collect(Collectors.toList());

        for (Entry<String, String> entry : messageFormats.entrySet()) {
            if (playerGroups.contains(entry.getKey())) {
                return entry.getValue();
            }
            //TODO: Check inheritance. (May have to link directly into permission plugins...)
        }

        return messageFormats.get("default");
    }

    public String formatMessage(Chatter sender, String message) {
        String finalMessage = getFormat(sender);

        Map<String, String> cachedReplacements = new HashMap<>();

        for (ChatVariable variable : ChatFormat.VARIABLES.values()) {
            String key = variable.getReplaceKey();
            if (!cachedReplacements.containsKey(key)) {
                String replacement = variable.getReplacement(sender, null);
                if (replacement == null) {
                    replacement = "";
                }
                cachedReplacements.put(key, replacement);
            }

            finalMessage = finalMessage.replace(key, cachedReplacements.get(key));
        }

        return ChatColor.translateAlternateColorCodes('&', finalMessage).replace("{MESSAGE}", ChatFormat.applyApplicableChatColors(sender, message));
    }

}