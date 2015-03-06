package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.utils.ChatColorUtils;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelManager extends FlexModule<FlexChat> implements Listener {

    public static enum VariableModifier {

        /**
         * Adds a space after a variable if the replacement is not null.
         */
        NON_NULL_SPACE("BUFFER");

        private VariableModifier(String shortCode) {
            VARIABLE_MODIFIERS.put(shortCode, this);
        }

    }

    private final static Map<String, VariableModifier> VARIABLE_MODIFIERS = new HashMap<>();

    public final static Pattern CHANNEL_NAME_PATTERN = Pattern.compile("(?i)^[a-z][\\-_a-z0-9]*|^[0-9]+[\\-_a-z][\\-_a-z0-9]*");
    public final static Pattern VARIABLE_PATTERN = Pattern.compile("(?i)\\{([a-z0-9]+)}");
    public final static Pattern VARIABLE_MODIFIERS_PATTERN = Pattern.compile("(?i)\\{([a-z0-9]+):([^}]+)}");

    /* Configuration values */
    private String defaultChannel; // The identifier of the default channel
    private String activeSymbol; // The symbol to use on the list command to mark a channel as being the active channel.

    /**
     * Registered {@link me.st28.flexseries.flexchat.api.ChatVariable}s.<br />
     * <b>Structure:</b> <code>raw key, ChatVariable</code>
     */
    private final Map<String, ChatVariable> variables = new HashMap<>();

    /**
     * Loaded channels.<br />
     * <b>Structure:</b> <code>channel identifier, channel</code>
     */
    private final Map<String, Channel> channels = new HashMap<>();

    /**
     * Registered channel permissions.
     */
    private final List<Permission> registeredPermissions = new ArrayList<>();

    /**
     * The directory to load channel data from.
     */
    private File channelDir;

    /**
     * The directory to load custom data from.
     */
    private File customChannelDir;

    public ChannelManager(FlexChat plugin) {
        super(plugin, "channels", "Manages chat channels and player chatting");

        registerChatVariable(new ChatVariable("NAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return chatter.getName();
            }
        });

        registerChatVariable(new ChatVariable("DISPNAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return chatter.getDisplayName();
            }
        });

        registerChatVariable(new ChatVariable("DISPNAMENC") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return ChatColor.stripColor(chatter.getDisplayName());
            }
        });

        registerChatVariable(new ChatVariable("WORLD") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (chatter instanceof PlayerChatter) {
                    return ((PlayerChatter) chatter).getPlayer().getWorld().getName();
                }
                return null;
            }
        });

        registerChatVariable(new ChatVariable("GROUP") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (chatter instanceof PlayerChatter) {
                    try {
                        return FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getPermission().getPrimaryGroup(null, ((PlayerChatter) chatter).getPlayer());
                    } catch (HookDisabledException ex) {
                    }
                }
                return null;
            }
        });

        registerChatVariable(new ChatVariable("PREFIX") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (chatter instanceof PlayerChatter) {
                    try {
                        return FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getChat().getPlayerPrefix(null, ((PlayerChatter) chatter).getPlayer());
                    } catch (HookDisabledException ex) {
                    }
                }
                return null;
            }
        });

        registerChatVariable(new ChatVariable("SUFFIX") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (chatter instanceof PlayerChatter) {
                    try {
                        return FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getChat().getPlayerSuffix(null, ((PlayerChatter) chatter).getPlayer());
                    } catch (HookDisabledException ex) {
                    }
                }
                return null;
            }
        });

        registerChatVariable(new ChatVariable("CHNAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return channel.getName();
            }
        });

        registerChatVariable(new ChatVariable("CHSHORTNAME") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return channel.getShortName();
            }
        });

        registerChatVariable(new ChatVariable("CHCOLOR") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                return channel.getColor().toString();
            }
        });
    }

    @Override
    protected void handleLoad() throws Exception {
        channelDir = new File(plugin.getDataFolder() + File.separator + "channels");
        customChannelDir = new File(channelDir + File.separator + "custom");
    }

    @Override
    protected void handleReload() {
        channelDir.mkdirs();
        customChannelDir.mkdirs();

        if (channelDir.listFiles().length == 1) {
            LogHelper.info(this, "No channels found in the channels directory. Creating a default channel file.");
            plugin.saveResource("channels" + File.separator + "default.yml", true);
        }

        FileConfiguration config = getConfig();
        defaultChannel = config.getString("default channel");
        activeSymbol = config.getString("active symbol", ">");

        //TODO: Reload channels
        List<String> configurableChannels = new ArrayList<>();
        for (Entry<String, Channel> entry : channels.entrySet()) {
            if (entry.getValue() instanceof ConfigurableChannel) {
                configurableChannels.add(entry.getKey());
            }
        }

        // Load normal channels
        for (File file : channelDir.listFiles()) {
            if (YamlFileManager.YAML_FILE_PATTERN.matcher(file.getName()).matches()) {
                String identifier = file.getName().replace(".yml", "");

                try {
                    if (configurableChannels.remove(identifier)) {
                        loadChannel((ConfigurableChannel) channels.get(identifier), new YamlFileManager(file));
                    } else {
                        ConfigurableChannel channel = new StandardChannel(identifier);
                        loadChannel(channel, new YamlFileManager(file));
                        channels.put(identifier, channel);
                    }
                } catch (Exception ex) {
                    LogHelper.severe(this, "An error occurred while loading channel configuration in file: " + file.getName());
                    ex.printStackTrace();
                }
            }
        }

        // Load custom channels
        for (File file : customChannelDir.listFiles()) {
            if (YamlFileManager.YAML_FILE_PATTERN.matcher(file.getName()).matches()) {
                String identifier = file.getName().replace(".yml", "");
            }
        }

        if (defaultChannel != null && !channels.containsKey(defaultChannel.toLowerCase())) {
            LogHelper.warning(this, "The default channel '" + defaultChannel + "' is not loaded.");
        }

        try {
            FlexPlugin.getRegisteredModule(ChatterManager.class).refreshChatters();
        } catch (ModuleDisabledException ex) { }
    }

    public void loadChannel(ConfigurableChannel channel, YamlFileManager file) {
        channel.data = new ChannelData(file);
    }

    /**
     * Registers a chat variable.
     * @throws java.lang.IllegalStateException Thrown if the variable is already registered.
     */
    public final void registerChatVariable(ChatVariable variable) {
        Validate.notNull(variable, "Variable cannot be null.");

        String key = variable.getRawKey().toLowerCase();
        if (variables.containsKey(key))
            throw new IllegalStateException("A chat variable with the key '" + key + "' is already registered.");

        variables.put(key, variable);
    }

    /**
     * @return the symbol used to signify that a channel is currently active.
     */
    public String getActiveSymbol() {
        return activeSymbol;
    }

    /**
     * @return the default channel.<br />
     *         Null if no default channel is set or there is no channel matching the default channel identifier.
     */
    public Channel getDefaultChannel() {
        return defaultChannel == null ? null : channels.get(defaultChannel);
    }

    /**
     * @return a channel matching the given identifier.
     */
    public Channel getChannel(String identifier) {
        Validate.notNull(identifier, "Identifier cannot be null.");
        return channels.get(identifier);
    }

    public Channel getChannelByName(String name) {
        Validate.notNull(name, "Name cannot be null.");

        for (Channel channel : channels.values()) {
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        }
        return null;
    }

    /**
     * @return a read-only collection of all loaded channels.
     */
    public Collection<Channel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;

        e.getRecipients().clear();
        e.setCancelled(true);

        Player p = e.getPlayer();
        Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(p);
        Channel channel = chatter.getActiveChannel();

        if (channel == null) {
            MessageReference.create(FlexChat.class, "errors.channel_none").sendTo(p);
            e.setCancelled(true);
            return;
        }

        if (!PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_CHAT, channel.getName().toLowerCase()).isAllowed(p)) {
            MessageReference.create(FlexChat.class, "errors.channel_no_permission", new QuickMap<>("{VERB}", "chat in").put("{CHANNEL}", channel.getName()).getMap()).sendTo(p);
            e.setCancelled(true);
            return;
        }

        String format = ChatColor.translateAlternateColorCodes('&', channel.getChatFormat(chatter));

        Matcher variableMatcher = VARIABLE_PATTERN.matcher(format);
        while (variableMatcher.find()) {
            ChatVariable variable = variables.get(variableMatcher.group(1).toLowerCase());
            if (variable != null) {
                String replacement = variable.getReplacement(chatter, channel);
                if (replacement == null || replacement.equals("")) {
                    format = format.replace(variableMatcher.group(), "");
                    continue;
                }

                format = format.replace(variableMatcher.group(), ChatColor.translateAlternateColorCodes('&', replacement));
            }
        }

        Matcher variableModifierMatcher = VARIABLE_MODIFIERS_PATTERN.matcher(format);
        while (variableModifierMatcher.find()) {
            ChatVariable variable = variables.get(variableModifierMatcher.group(1).toLowerCase());
            if (variable != null) {
                String[] modifiers = variableModifierMatcher.group(2).split(",");

                String replacement = variable.getReplacement(chatter, channel);
                if (replacement == null || replacement.equals("")) {
                    format = format.replace(variableModifierMatcher.group(), "");
                    continue;
                }

                for (String modifier : modifiers) {
                    try {
                        switch (VARIABLE_MODIFIERS.get(modifier.toUpperCase())) {
                            case NON_NULL_SPACE:
                                replacement = replacement + " ";
                                break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
                format = format.replace(variableModifierMatcher.group(), ChatColor.translateAlternateColorCodes('&', replacement));
            }
        }

        String message = e.getMessage();

        if (PermissionNodes.CHAT_COLOR.isAllowed(p)) {
            message = ChatColorUtils.colorizeString(message);
        }

        if (PermissionNodes.CHAT_FORMAT.isAllowed(p)) {
            message = ChatColorUtils.formatString(message);
        }

        if (PermissionNodes.CHAT_MAGIC.isAllowed(p)) {
            message = ChatColorUtils.magicfyString(message);
        }

        format = format.replace("{MESSAGE}", message);

        MessageReference messageRef = MessageReference.createPlain(format);
        for (Chatter curChatter : channel.getRecipients(chatter)) {
            curChatter.sendMessage(messageRef);
        }
    }

}