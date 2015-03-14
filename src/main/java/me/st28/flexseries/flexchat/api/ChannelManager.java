package me.st28.flexseries.flexchat.api;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.backend.towny.TownyListener;
import me.st28.flexseries.flexchat.backend.towny.TownyNationChannel;
import me.st28.flexseries.flexchat.backend.towny.TownyTownChannel;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.JobsHook;
import me.st28.flexseries.flexcore.hooks.TownyHook;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.utils.ChatColorUtils;
import me.st28.flexseries.flexcore.utils.PluginUtils;
import me.st28.flexseries.flexcore.utils.QuickMap;
import me.zford.jobs.Jobs;
import me.zford.jobs.container.JobsPlayer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelManager extends FlexModule<FlexChat> implements Listener {

    public static String applyApplicableChatColors(CommandSender sender, String input) {
        if (PermissionNodes.CHAT_COLOR.isAllowed(sender)) {
            input = ChatColorUtils.colorizeString(input);
        }

        if (PermissionNodes.CHAT_FORMAT.isAllowed(sender)) {
            input = ChatColorUtils.formatString(input);
        }

        if (PermissionNodes.CHAT_MAGIC.isAllowed(sender)) {
            input = ChatColorUtils.magicfyString(input);
        }
        return input;
    }

    public static enum VariableModifier {

        /**
         * Optional format for the variable.
         */
        FORMAT("FORMAT"),

        /**
         * Adds a space after a variable if the replacement is not null.
         */
        NON_NULL_SPACE("BUFFER");

        private VariableModifier(String shortCode) {
            VARIABLE_MODIFIERS.put(shortCode, this);
        }

    }

    private final static Map<String, VariableModifier> VARIABLE_MODIFIERS = new HashMap<>();

    public final static Pattern CHAT_COLOR_PATTERN = Pattern.compile("(?i)&[a-fl-okr0-9]");
    public final static Pattern CHANNEL_NAME_PATTERN = Pattern.compile("(?i)^[a-z][\\-_a-z0-9]*|^[0-9]+[\\-_a-z][\\-_a-z0-9]*");
    public final static Pattern VARIABLE_PATTERN = Pattern.compile("(?i)\\{([a-z0-9-]+)}");
    public final static Pattern VARIABLE_MODIFIERS_PATTERN = Pattern.compile("(?i)\\{([a-z0-9-]+):([^}]+)}");

    /* Configuration values */
    private String activeSymbol; // The symbol to use on the list command to mark a channel as being the active channel.
    private String privateMessageFormat; // The format to use for private messages.
    private String defaultChannel; // The identifier of the default channel.

    /**
     * Registered {@link me.st28.flexseries.flexchat.api.ChatVariable}s.<br />
     * <b>Structure:</b> <code>raw key, ChatVariable</code>
     */
    private final Map<String, ChatVariable> variables = new HashMap<>();

    /**
     * Formats for variables.<br />
     * <b>Structure:</b> <code>name, modifier</code>
     */
    private final Map<String, String> variableFormats = new HashMap<>();

    /**
     * The identifiers of custom registered {@link me.st28.flexseries.flexchat.api.Channel}s.
     */
    private final Set<String> customChannelIdentifiers = new HashSet<>();

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
        super(plugin, "channels", "Manages chat channels and player chatting", false);

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
        customChannelDir.mkdirs();

        try {
            FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(JobsHook.class);

            registerChatVariable(new ChatVariable("JOBS-JOBS") {
                @Override
                public String getReplacement(Chatter chatter, Channel channel) {
                    if (!(chatter instanceof PlayerChatter)) {
                        return null;
                    }

                    JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(((PlayerChatter) chatter).getPlayer());
                    if (jPlayer == null) {
                        return null;
                    }

                    return jPlayer.getDisplayHonorific();
                }
            });

            LogHelper.info(this, "Optional features for Jobs enabled.");
        } catch (HookDisabledException | ModuleDisabledException ex) {
            LogHelper.info(this, "Unable to register optional features for Jobs because it isn't installed.");
        }

        try {
            FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(TownyHook.class);

            registerCustomChannel(plugin, new TownyTownChannel());
            registerCustomChannel(plugin, new TownyNationChannel());

            registerChatVariable(new ChatVariable("TOWNY-TOWN") {
                @Override
                public String getReplacement(Chatter chatter, Channel channel) {
                    if (!(chatter instanceof PlayerChatter)) {
                        return null;
                    }

                    try {
                        return TownyUniverse.getDataSource().getResident(chatter.getName()).getTown().getName();
                    } catch (NotRegisteredException ex) {
                        return null;
                    }
                }
            });

            registerChatVariable(new ChatVariable("TOWNY-NATION") {
                @Override
                public String getReplacement(Chatter chatter, Channel channel) {
                    if (!(chatter instanceof PlayerChatter)) {
                        return null;
                    }

                    try {
                        Town town = TownyUniverse.getDataSource().getResident(chatter.getName()).getTown();

                        try {
                            return town.getNation().getName();
                        } catch (NotRegisteredException ex) {
                            return null;
                        }
                    } catch (NotRegisteredException ex) {
                        return null;
                    }
                }
            });

            Bukkit.getPluginManager().registerEvents(new TownyListener(), plugin);
            LogHelper.info(this, "Optional features for Towny enabled.");
        } catch (HookDisabledException | ModuleDisabledException ex) {
            LogHelper.info(this, "Unable to register optional features for Towny because it isn't installed.");
        }
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
        activeSymbol = StringEscapeUtils.unescapeJava(config.getString("active symbol", ">"));
        privateMessageFormat = StringEscapeUtils.unescapeJava(config.getString("private message format", "&7{SENDER} &f\u27A1 &7{RECEIVER} &8> &7{MESSAGE}"));

        variableFormats.clear();
        ConfigurationSection formatSec = config.getConfigurationSection("variable formats");
        if (formatSec != null) {
            for (String name : formatSec.getKeys(false)) {
                variableFormats.put(name.toLowerCase(), formatSec.getString(name));
            }
        }

        List<String> configurableChannels = new ArrayList<>();
        for (Entry<String, Channel> entry : channels.entrySet()) {
            if (entry.getValue() instanceof ConfigurableChannel && !customChannelIdentifiers.contains(entry.getKey())) {
                configurableChannels.add(entry.getKey());
            }
        }

        //TODO: Register channel permissions

        // Load normal channels
        for (File file : channelDir.listFiles()) {
            if (YamlFileManager.YAML_FILE_PATTERN.matcher(file.getName()).matches()) {
                String identifier = file.getName().replace(".yml", "");

                if (customChannelIdentifiers.contains(identifier)) {
                    continue;
                }

                try {
                    if (configurableChannels.remove(identifier)) {
                        loadChannel((ConfigurableChannel) channels.get(identifier), new YamlFileManager(file));
                    } else {
                        ConfigurableChannel channel = new StandardChannel(identifier);
                        channels.put(identifier, channel);
                        loadChannel(channel, new YamlFileManager(file));
                        LogHelper.info(this, "Loaded channel '" + identifier + "'");
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

                try {
                    Channel custChannel = channels.get(identifier);

                    if (custChannel != null && custChannel instanceof ConfigurableChannel) {
                        loadChannel((ConfigurableChannel) custChannel, new YamlFileManager(file));
                        LogHelper.info(this, "Loaded data for custom channel identified by '" + identifier + "'");
                    }
                } catch (Exception ex) {
                    LogHelper.severe(this, "An error occurred while loading channel configuration in file: " + file.getName());
                    ex.printStackTrace();
                }
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
        //channel.data = new ChannelData(file);
        channel.setData(new ChannelData(file));
    }

    /**
     * Registers a custom implementation of a channel.
     * @throws java.lang.IllegalStateException Thrown if a channel with the same identifier is already registered.
     */
    public final void registerCustomChannel(JavaPlugin plugin, Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");

        String identifier = channel.getIdentifier();
        if (channels.containsKey(identifier)) {
            throw new IllegalStateException("A channel identified by '" + identifier + "' is already registered.");
        }

        channels.put(identifier, channel);
        customChannelIdentifiers.add(identifier);

        if (channel instanceof ConfigurableChannel) {
            String fileName = identifier + ".yml";
            if (!new File(customChannelDir + File.separator + fileName).exists()) {
                try {
                    if (!PluginUtils.saveFile(plugin, "channels" + File.separator + "custom" + File.separator + fileName, customChannelDir + File.separator + fileName)) {
                        LogHelper.warning(this, "No default file for channel '" + identifier + "' found in plugin '" + plugin.getName() + "'");
                    } else {
                        LogHelper.info(this, "Created default channel file for channel '" + identifier + "' from plugin '" + plugin.getName() + "'");
                    }
                } catch (Exception ex) {
                    LogHelper.warning(this, "An exception occurred while trying to copy the channel file for custom channel '" + identifier + "'");
                    ex.printStackTrace();
                }
            }
        }
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
        LogHelper.debug(this, "Registered chat variable '" + variable.getRawKey() + "'");
    }

    /**
     * @return the symbol used to signify that a channel is currently active.
     */
    public String getActiveSymbol() {
        return activeSymbol;
    }

    /**
     * @return the format to use for the private messaging command.
     */
    public String getPrivateMessageFormat() {
        return privateMessageFormat;
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

        if (!channel.hasOwnPermissions() && !PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_CHAT, channel.getName().toLowerCase()).isAllowed(p)) {
            MessageReference.create(FlexChat.class, "errors.channel_no_permission", new QuickMap<>("{VERB}", "chat in").put("{CHANNEL}", channel.getName()).getMap()).sendTo(p);
            e.setCancelled(true);
            return;
        }

        String format = handleReplacements(chatter, channel, ChatColor.translateAlternateColorCodes('&', channel.getChatFormat(chatter)));
        String message = applyApplicableChatColors(p, e.getMessage());
        message = CHAT_COLOR_PATTERN.matcher(message).replaceAll("");

        format = format.replace("{MESSAGE}", message);

        String senderIdentifier = chatter.getIdentifier();
        boolean isBypassingIgnore = PermissionNodes.IGNORE_BYPASS.isAllowed(p);

        MessageReference messageRef = MessageReference.createPlain(format);
        for (Chatter curChatter : channel.getRecipients(chatter)) {
            if (isBypassingIgnore || !curChatter.getIgnored().contains(senderIdentifier)) {
                curChatter.sendMessage(messageRef);
            }
        }

        FlexChat.CHAT_LOGGER.log(Level.INFO, "[[" + channel.getName() + "]] " + ChatColor.stripColor(handleReplacements(chatter, channel, channel.getLogFormat()).replace("{MESSAGE}", message)));
    }

    private String handleReplacements(Chatter chatter, Channel channel, String input) {
        Matcher variableMatcher = VARIABLE_PATTERN.matcher(input);
        while (variableMatcher.find()) {
            ChatVariable variable = variables.get(variableMatcher.group(1).toLowerCase());
            if (variable != null) {
                String replacement = variable.getReplacement(chatter, channel);
                if (replacement == null || replacement.equals("")) {
                    input = input.replace(variableMatcher.group(), "");
                    continue;
                }

                input = input.replace(variableMatcher.group(), ChatColor.translateAlternateColorCodes('&', replacement));
            }
        }

        Matcher variableModifierMatcher = VARIABLE_MODIFIERS_PATTERN.matcher(input);
        while (variableModifierMatcher.find()) {
            ChatVariable variable = variables.get(variableModifierMatcher.group(1).toLowerCase());
            if (variable != null) {
                String[] modifiers = variableModifierMatcher.group(2).split(",");

                String replacement = variable.getReplacement(chatter, channel);
                if (replacement == null || replacement.equals("")) {
                    input = input.replace(variableModifierMatcher.group(), "");
                    continue;
                }

                for (String modifier : modifiers) {
                    String[] split = modifier.split("=");
                    String key = split[0];
                    String value = split.length == 1 ? null : split[1].toLowerCase();

                    try {
                        switch (VARIABLE_MODIFIERS.get(key.toUpperCase())) {
                            case FORMAT:
                                if (value == null || !variableFormats.containsKey(value)) {
                                    continue;
                                }

                                replacement = variableFormats.get(value).replace("{VARIABLE}", replacement);
                                break;

                            case NON_NULL_SPACE:
                                replacement = replacement + " ";
                                break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
                input = input.replace(variableModifierMatcher.group(), ChatColor.translateAlternateColorCodes('&', replacement));
            }
        }

        return input;
    }

}