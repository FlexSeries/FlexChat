package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.events.ChannelActiveSetEvent;
import me.st28.flexseries.flexchat.api.events.ChannelJoinEvent;
import me.st28.flexseries.flexchat.api.events.ChannelLeaveEvent;
import me.st28.flexseries.flexchat.backend.hooks.TownyNationChannel;
import me.st28.flexseries.flexchat.backend.hooks.TownyTownChannel;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.hooks.Hook;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.JobsHook;
import me.st28.flexseries.flexcore.hooks.TownyHook;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.utils.*;
import me.zford.jobs.Jobs;
import me.zford.jobs.container.JobsPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelManager extends FlexModule<FlexChat> implements Listener, PlayerLoader {

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

    private String defaultChannel;
    private String activeSymbol;

    /* Channels that were loaded from the channels directory */
    private final Set<String> loadedChannels = new HashSet<>();
    private final Map<String, Channel> channels = new HashMap<>();
    private final Map<String, Chatter> chatters = new HashMap<>();
    private final Map<Chatter, Set<Channel>> chatterChannels = new HashMap<>();
    private final Map<Chatter, Channel> chatterActiveChannels = new HashMap<>();

    private final Map<String, ChatVariable> variables = new HashMap<>();

    private final List<Permission> registeredPermissions = new ArrayList<>();

    private File chatterDir;
    private File channelDir;

    public ChannelManager(FlexChat plugin) {
        super(plugin, "channels", "Manages channels and player chatting", PlayerManager.class);

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
        chatterDir = new File(plugin.getDataFolder() + File.separator + "chatters");
        channelDir = new File(plugin.getDataFolder() + File.separator + "channels");

        loadChatter(ConsoleChatter.NAME);

        /*try {
            FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(TownyHook.class);

            registerChannel(new TownyTownChannel());
            registerChannel(new TownyNationChannel());
        } catch (Exception ex) {
            LogHelper.info(this, "Unable to register Towny features because Towny isn't installed.");
        }*/

        try {
            FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(JobsHook.class);

            registerChatVariable(new ChatVariable("JOBS") {
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

            LogHelper.info(this, "Chat variables for Jobs enabled.");
        } catch (HookDisabledException ex) {
            LogHelper.debug(this, "Unable to register Jobs chat variables because Jobs isn't installed.");
        }
    }

    @Override
    protected void handleReload() {
        chatterDir.mkdirs();
        channelDir.mkdirs();

        final Map<Chatter, String> prevActiveChannels = new HashMap<>();
        final Map<Chatter, Set<String>> prevChannels = new HashMap<>();
        for (Entry<Chatter, Channel> entry : chatterActiveChannels.entrySet()) {
            prevActiveChannels.put(entry.getKey(), entry.getValue().getName().toLowerCase());
        }
        chatterActiveChannels.clear();

        for (Entry<Chatter, Set<Channel>> entry : chatterChannels.entrySet()) {
            prevChannels.put(entry.getKey(), StringUtils.collectionToStringCollection(entry.getValue(), new StringConverter<Channel>() {
                @Override
                public String toString(Channel object) {
                    return object.getName().toLowerCase();
                }
            }, new HashSet<String>()));
        }
        chatterChannels.clear();

        for (Permission permission : registeredPermissions) {
            Bukkit.getPluginManager().removePermission(permission);
        }
        registeredPermissions.clear();

        for (String loadedChannel : loadedChannels) {
            channels.remove(loadedChannel);
        }
        loadedChannels.clear();

        for (File file : channelDir.listFiles()) {
            if (YamlFileManager.YAML_FILE_PATTERN.matcher(file.getName()).matches()) {
                try {
                    Channel channel = loadChannel(new YamlFileManager(file));
                    registerChannel(channel);
                    loadedChannels.add(channel.getName().toLowerCase());
                } catch (Exception ex) {
                    LogHelper.warning(this, "An exception occurred while loading channel from file: " + file.getName());
                    ex.printStackTrace();
                }
            }
        }

        FileConfiguration config = getConfig();

        defaultChannel = config.getString("Default channel", "default").toLowerCase();
        if (!channels.containsKey(defaultChannel)) {
            LogHelper.warning(this, "There is no loaded channel matching the default channel defined in the config ('" + defaultChannel + "')");
        }
        activeSymbol = config.getString("Active symbol", "\u25B6");

        for (Channel channel : channels.values()) {
            if (loadedChannels.contains(channel.getName().toLowerCase())) {
                continue;
            }
            registerChannelPermissions(channel);
        }

        for (Entry<Chatter, Set<String>> entry : prevChannels.entrySet()) {
            Set<Channel> entryChannels = new HashSet<>();
            chatterChannels.put(entry.getKey(), entryChannels);

            for (String channelName : entry.getValue()) {
                Channel channel = channels.get(channelName.toLowerCase());
                if (channel != null) {
                    entryChannels.add(channel);
                }
            }
        }

        for (Entry<Chatter, String> entry : prevActiveChannels.entrySet()) {
            Channel channel = channels.get(entry.getValue().toLowerCase());
            if (channel != null) {
                chatterActiveChannels.put(entry.getKey(), channel);
            }
        }
    }

    private void loadChatter(String identifier) {
        if (chatters.containsKey(identifier)) {
            return;
        }

        Chatter chatter;

        YamlFileManager file = new YamlFileManager(chatterDir + File.separator + identifier + ".yml");
        FileConfiguration config = file.getConfig();
        if (identifier.equals(ConsoleChatter.NAME)) {
            chatters.put(ConsoleChatter.NAME, chatter = new ConsoleChatter());
        } else {
            UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));

            chatters.put(identifier, chatter = new PlayerChatter(uuid));
        }

        String activeChannelName = config.getString(Chatter.CONFIG_ACTIVE_CHANNEL, null);
        if (activeChannelName != null && !channels.containsKey(activeChannelName.toLowerCase())) {
            LogHelper.warning(this, "Chatter '" + chatter.getName() + "' has its active channel set to a non-existent channel ('" + activeChannelName + "'");
            activeChannelName = null;
        }

        if (activeChannelName != null) {
            chatter.setActiveChannel(channels.get(activeChannelName.toLowerCase()));
        }

        Set<Channel> curChannels = chatterChannels.get(chatter);
        if (curChannels == null) {
            chatterChannels.put(chatter, curChannels = new HashSet<>());
        }

        List<String> channelNames = config.getStringList(Chatter.CONFIG_CHANNELS);
        for (String channelName : channelNames) {
            Channel channel = channels.get(channelName.toLowerCase());

            if (channel != null) {
                curChannels.add(channel);
            }
        }
    }

    private FileChannel loadChannel(YamlFileManager file) {
        String rawName = file.getName().replace(".yml", "");

        if (!CHANNEL_NAME_PATTERN.matcher(rawName).matches()) {
            throw new IllegalArgumentException("Invalid channel name '" + rawName + "' from file: " + file.getName());
        }

        FileConfiguration config = file.getConfig();
        ChatColor color = ChatColor.valueOf(config.getString("color", "WHITE").toUpperCase());
        List<String> banned = config.getStringList("banned");

        // Load the formats
        ConfigurationSection formatSec = config.getConfigurationSection("formats");
        if (formatSec == null || formatSec.getKeys(false).isEmpty()) {
            throw new IllegalArgumentException("No formats are defined in channel file: " + file.getFile().getName());
        }

        Map<String, String> formats = new HashMap<>();
        for (String group : formatSec.getKeys(false)) {
            String key = group.toLowerCase();
            if (!formats.containsKey(key)) {
                formats.put(key, formatSec.getString(group));
            } else {
                LogHelper.warning(plugin, "Channel '" + rawName + "' already has a format set for group: " + key);
            }
        }

        if (formats.isEmpty()) {
            throw new IllegalStateException("No valid formats were loaded for channel: " + rawName);
        }

        if (!formats.containsKey("default")) {
            throw new IllegalArgumentException("Channel '" + rawName + "' does not have a default format set.");
        }

        loadedChannels.add(rawName);
        String shortName = config.getString("shortName", null);

        switch (rawName.toLowerCase()) {
            case "towny-town":
                FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(TownyHook.class);
                return new TownyTownChannel(shortName, color, banned, formats);

            case "towny-nation":
                FlexPlugin.getRegisteredModule(HookManager.class).checkHookStatus(TownyHook.class);
                return new TownyNationChannel(shortName, color, banned, formats);
        }
        return new FileChannel(rawName, shortName, color, banned, formats);
    }

    public boolean registerChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");

        String identifier = channel.getName().toLowerCase();

        if (channels.containsKey(identifier)) {
            return false;
        }

        registerChannelPermissions(channel);

        channels.put(identifier, channel);
        LogHelper.debug(this, "Registered channel '" + identifier + "'");
        return true;
    }

    private void registerChannelPermissions(Channel channel) {
        String identifier = channel.getName().toLowerCase();

        PluginManager pm = Bukkit.getPluginManager();

        Permission chatPerm = new Permission(PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_CHAT, identifier).getNode(), PermissionDefault.TRUE);
        Permission joinPerm = new Permission(PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_JOIN, identifier).getNode(), PermissionDefault.TRUE);
        Permission leavePerm = new Permission(PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_LEAVE, identifier).getNode(), PermissionDefault.TRUE);
        Permission viewPerm = new Permission(PermissionNodes.buildVariableNode(PermissionNodes.CHANNEL_VIEW, identifier).getNode(), PermissionDefault.TRUE);

        pm.addPermission(chatPerm);
        pm.addPermission(joinPerm);
        pm.addPermission(leavePerm);
        pm.addPermission(viewPerm);

        registeredPermissions.add(chatPerm);
        registeredPermissions.add(joinPerm);
        registeredPermissions.add(leavePerm);
        registeredPermissions.add(viewPerm);
    }

    @Override
    protected void handleSave(boolean async) {
        for (Chatter chatter : chatters.values()) {
            saveChatter(chatter);
        }
    }

    public void saveChatter(Chatter chatter) {
        YamlFileManager file = new YamlFileManager(chatterDir + File.separator + chatter.getIdentifier() + ".yml");
        FileConfiguration config = file.getConfig();

        Channel activeChannel = chatter.getActiveChannel();
        config.set(Chatter.CONFIG_ACTIVE_CHANNEL, activeChannel == null ? null : activeChannel.getName());

        List<String> channelNames = new ArrayList<>();
        for (Channel channel : chatter.getChannels()) {
            channelNames.add(channel.getName());
        }
        config.set(Chatter.CONFIG_CHANNELS, channelNames);

        file.save();
    }

    @Override
    public boolean isPlayerLoadSync() {
        return false;
    }

    @Override
    public boolean loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        if (!cycle.isLoaderComplete(PlayerManager.class)) {
            return false;
        }

        loadChatter(uuid.toString());
        PlayerLoadCycle.completedCycle(cycle, this);
        return true;
    }

    /**
     * Registers a chat variable.
     *
     * @param variable The chat variable to register.  Must not be null.
     * @throws java.lang.IllegalStateException Thrown if a chat variable with the same key is already registered.
     */
    public void registerChatVariable(ChatVariable variable) {
        Validate.notNull(variable, "Variable cannot be null.");

        String key = variable.getKey().toLowerCase().replace("{", "").replace("}", "");
        if (variables.containsKey(key))
            throw new IllegalStateException("A chat variable with the key '" + key + "' is already registered.");

        variables.put(key, variable);
    }

    public String getActiveChannelSymbol() {
        return activeSymbol;
    }

    /**
     * @return the default chat channel.<br />
     *         Null if there is no default chat channel.
     */
    public Channel getDefaultChannel() {
        return channels.get(defaultChannel);
    }

    /**
     * Retrieves a channel based on name.
     *
     * @param name The name of the channel to retrieve.  Must not be null.
     * @return A channel matching the given name.<br />
     *         Null if no channel with the given name was found.
     */
    public Channel getChannel(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return channels.get(name.toLowerCase());
    }

    /**
     * @return an unmodifiable collection of all loaded channels.
     */
    public Collection<Channel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    public Chatter getChatter(String identifier) {
        Validate.notNull(identifier, "Identifier cannot be null.");
        return chatters.get(identifier);
    }

    public Chatter getChatter(CommandSender sender) {
        Validate.notNull(sender, "Sender cannot be null.");
        if (sender instanceof Player) {
            return chatters.get(((Player) sender).getUniqueId().toString());
        } else if (sender instanceof ConsoleCommandSender) {
            return chatters.get(ConsoleChatter.NAME);
        } else {
            return null;
        }
    }

    /**
     * @return an unmodifiable collection of all loaded chatters.
     */
    public Collection<Chatter> getChatters() {
        return Collections.unmodifiableCollection(chatters.values());
    }

    /**
     * @return a collection of channels that a particular chatter has joined.
     */
    public Collection<Channel> getChatterChannels(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");
        return Collections.unmodifiableCollection(chatterChannels.containsKey(chatter) ? chatterChannels.get(chatter) : new HashSet<Channel>());
    }

    public Channel getChatterActiveChannel(Chatter chatter) {
        Validate.notNull(chatter, "Chatter cannot be null.");
        return chatterActiveChannels.get(chatter);
    }

    /**
     * Adds a chatter to a channel.
     *
     * @return A DynamicResponse representing true if the chatter was successfully added.
     */
    public DynamicResponse addChatterToChannel(Channel channel, Chatter chatter) {
        Validate.notNull(channel, "Channel cannot be null.");
        Validate.notNull(chatter, "Chatter cannot be null.");

        DynamicResponse response = channel.canChatterJoin(chatter);
        if (!response.isSuccess()) {
            return response;
        }

        // Make sure the chatter isn't already in the channel.
        if (chatter.getChannels().contains(channel)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_already_joined", new QuickMap<>("{CHANNEL}", channel.getName()).getMap()));
        }

        Set<Channel> curChannels = chatterChannels.get(chatter);
        if (curChannels == null) {
            chatterChannels.put(chatter, curChannels = new HashSet<>());
        }

        Bukkit.getPluginManager().callEvent(new ChannelJoinEvent(channel, chatter));
        curChannels.add(channel);
        channel.sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_joined", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_joined", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
    }

    /**
     * Sets a chatter's active channel.<br />
     * If the chatter hasn't already  joined the channel, an attempt will be made to add them to the channel via #addChatterToChannel
     *
     * @return A DynamicResponse representing true if the active channel was successfully set.
     */
    public DynamicResponse setChatterActiveChannel(Channel channel, Chatter chatter) {
        Validate.notNull(channel, "Channel cannot be null.");
        Validate.notNull(chatter, "Chatter cannot be null.");

        DynamicResponse response = null;
        if (!getChatterChannels(chatter).contains(channel)) {
            response = addChatterToChannel(channel, chatter);
            if (!response.isSuccess()) {
                return response;
            }
        }

        if (getChatterActiveChannel(chatter) == channel) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_active_already_set", new QuickMap<>("{CHANNEL}", channel.getName()).getMap()));
        }

        chatterActiveChannels.put(chatter, channel);
        Bukkit.getPluginManager().callEvent(new ChannelActiveSetEvent(channel, chatter));

        if (response != null) {
            return new DynamicResponse(true, response.getMessages()[0], MessageReference.create(FlexChat.class, "notices.channel_active_set", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        } else {
            return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_active_set", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        }
    }

    /**
     * Removes a chatter from a channel.
     *
     * @return A DynamicResponse representing true if the chatter was successfully removed.
     */
    public DynamicResponse removeChatterFromChannel(Channel channel, Chatter chatter) {
        Validate.notNull(channel, "Channel cannot be null.");
        Validate.notNull(chatter, "Chatter cannot be null.");

        if (!chatter.getChannels().contains(channel)) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "errors.channel_not_joined", new QuickMap<>("{CHANNEL}", channel.getName()).getMap()));
        }

        Bukkit.getPluginManager().callEvent(new ChannelLeaveEvent(channel, chatter));
        chatterChannels.get(chatter).remove(channel);
        if (chatterActiveChannels.get(chatter) == channel) {
            //TODO: Go to next channel first
            chatterActiveChannels.remove(chatter);
        }
        channel.sendMessage(chatter, MessageReference.create(FlexChat.class, "notices.channel_chatter_left", new QuickMap<>("{CHATTER}", chatter.getDisplayName()).put("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        return new DynamicResponse(true, MessageReference.create(FlexChat.class, "notices.channel_left", new QuickMap<>("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerJoinLoadedEvent e) {
        Chatter chatter = getChatter(e.getPlayer().getUniqueId().toString());

        if ((boolean) e.getCustomData().get("firstJoin")) {
            Channel defaultChannel = getDefaultChannel();
            if (defaultChannel != null) {
                setChatterActiveChannel(defaultChannel, chatter);
            }
        }

        Channel channel = chatter.getActiveChannel();

        if (channel != null) {
            e.addLoginMessage(FlexChat.class, "channel", MessageReference.create(FlexChat.class, "notices.login_channel", new ReplacementMap("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent e) {
        Chatter chatter = getChatter(e.getPlayer());
        if (chatter != null) {
            saveChatter(chatter);

            chatters.remove(chatter.getIdentifier());
            chatterChannels.remove(chatter);
            chatterActiveChannels.remove(chatter);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatInitial(AsyncPlayerChatEvent e) {
        Chatter chatter = getChatter(e.getPlayer());

        if (chatter != null) {
            Channel channel = chatter.getActiveChannel();
            if (channel != null) {
                e.setFormat(channel.getFormat(chatter));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        e.getRecipients().clear();
        e.setCancelled(true);

        Player p = e.getPlayer();
        Chatter chatter = getChatter(p);
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

        String format = ChatColor.translateAlternateColorCodes('&', e.getFormat());

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

        for (Chatter curChatter : channel.getRecipients(chatter)) {
            curChatter.sendMessage(format);
        }
    }

}