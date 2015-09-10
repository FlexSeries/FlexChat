/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexchat.backend.channel;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChatVariable;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.channel.ChannelManager;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.api.format.StandardChatFormat;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.hooks.towny.TownyListener;
import me.st28.flexseries.flexchat.hooks.towny.TownyNationChannel;
import me.st28.flexseries.flexchat.hooks.towny.TownyTownChannel;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.list.ListManager;
import me.st28.flexseries.flexlib.player.PlayerExtendedJoinEvent;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.plugin.module.ModuleDisabledException;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexlib.utils.PluginUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class ChannelManagerImpl extends FlexModule<FlexChat> implements ChannelManager, Listener {

    public static final String DEFAULT_FORMAT = "{CHCOLOR}[{CHTAG}]{PREFIX}{DISPNAME}{SUFFIX}&f: {MESSAGE}";
    public static final Pattern GLOBAL_FORMAT_PATTERN = Pattern.compile("\\[g:(\\S+)\\]");

    private boolean firstReload = true;

    private String activeSymbol;

    private final Map<String, ChatFormat> globalFormats = new LinkedHashMap<>();

    private String defaultChannel;

    private final Set<String> loadedChannels = new HashSet<>();
    private final Map<String, Channel> channels = new HashMap<>();

    private File customChannelDir;

    public ChannelManagerImpl(FlexChat plugin) {
        super(plugin, "channels", "Manages channels", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleEnable() {
        customChannelDir = new File(getDataFolder() + File.separator + "custom");
        customChannelDir.mkdir();

        if (!setupHookTowny()) {
            LogHelper.info(this, "Unable to register optional features for Towny because it isn't installed.");
        }
    }

    private boolean setupHookTowny() {
        if (Bukkit.getPluginManager().getPlugin("Towny") == null) {
            return false;
        }

        TownyTownChannel townChannel = new TownyTownChannel();
        TownyNationChannel nationChannel = new TownyNationChannel();

        registerChannel(townChannel);
        registerChannel(nationChannel);

        ChatFormat.registerChatVariable(new ChatVariable("TOWNY-TOWN") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (!(chatter instanceof ChatterPlayer)) {
                    return null;
                }

                try {
                    return TownyUniverse.getDataSource().getResident(chatter.getName()).getTown().getName();
                } catch (NotRegisteredException ex) {
                    return null;
                }
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("TOWNY-TOWNTAG") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (!(chatter instanceof ChatterPlayer)) {
                    return null;
                }

                try {
                    return TownyUniverse.getDataSource().getResident(chatter.getName()).getTown().getTag();
                } catch (NotRegisteredException ex) {
                    return null;
                }
            }
        });

        ChatFormat.registerChatVariable(new ChatVariable("TOWNY-NATION") {
            @Override
            public String getReplacement(Chatter chatter, Channel channel) {
                if (!(chatter instanceof ChatterPlayer)) {
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

        Bukkit.getPluginManager().registerEvents(new TownyListener(townChannel, nationChannel), plugin);
        LogHelper.info(this, "Optional features for Towny enabled.");
        return true;
    }

    @Override
    protected void handleReload() {
        customChannelDir.mkdir();

        FileConfiguration config = getConfig();

        activeSymbol = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(config.getString("active symbol", "\u25B6")));
        FlexPlugin.getGlobalModule(ListManager.class).createElementFormat("flexchat_channel", "&a{ACTIVE}{COLOR}{CHANNEL} &8({STATUS}&8)");

        // Load formats
        globalFormats.clear();
        ConfigurationSection globalFormatSec = config.getConfigurationSection("global formats");

        for (String group : globalFormatSec.getKeys(false)) {
            ChatFormat format;
            try {
                format = ChatFormat.getChatFormat(this, globalFormatSec, group);
            } catch (Exception ex) {
                LogHelper.warning(this, "Unable to load global chat format for group '" + group + "'", ex);
                continue;
            }

            globalFormats.put(group.toLowerCase(), format);
            LogHelper.debug(this, "Loaded global format for group '" + group.toLowerCase() + "'");
        }

        if (!globalFormats.containsKey("default")) {
            globalFormats.put("default", new StandardChatFormat(DEFAULT_FORMAT, true));
        }

        // Load channels
        defaultChannel = config.getString("default channel");

        File channelDir = getDataFolder();

        if (channelDir.listFiles().length == 1) {
            LogHelper.info(this, "No channels found in the channels directory. Creating a default channel file.");
            plugin.saveResource("channels" + File.separator + "default.yml", true);
        }

        List<String> newLoadedChannels = new ArrayList<>();
        for (File file : channelDir.listFiles((dir, name) -> YamlFileManager.YAML_FILE_PATTERN.matcher(name).matches())) {
            YamlFileManager yaml = new YamlFileManager(file);
            String name = yaml.getConfig().getString("name");
            if (name == null) {
                LogHelper.warning(this, "Invalid channel file '" + file.getName() + "': no name defined");
                continue;
            }
            name = name.toLowerCase();

            if (channels.containsKey(name)) {
                channels.get(name).reload(this, yaml.getConfig());
                newLoadedChannels.add(name);
                continue;
            }

            Channel channel;
            try {
                channel = new StandardChannel(name);
                channel.reload(this, yaml.getConfig());
            } catch (Exception ex) {
                LogHelper.warning(this, "An exception occurred while loading channel '" + name + "'", ex);
                continue;
            }

            newLoadedChannels.add(name);
            channels.put(name, channel);

            if (!loadedChannels.contains(name)) {
                registerPermissions(name);
            }
        }

        if (!firstReload) {
            ChatterManagerImpl chatterManager = FlexPlugin.getGlobalModule(ChatterManagerImpl.class);

            for (String channel : loadedChannels) {
                if (!newLoadedChannels.contains(channel)) {
                    // Remove chatters from obsolete channel's instance(s)
                    for (Chatter chatter : chatterManager.getChatters()) {
                        for (ChannelInstance instance : chatter.getInstances()) {
                            if (instance.getChannel().getName().equals(channel)) {
                                chatter.removeInstance(instance);
                            }
                        }
                    }

                    // Remove permissions
                    unregisterPermission(channel);
                }
            }

            loadedChannels.clear();
            loadedChannels.addAll(newLoadedChannels);
        } else {
            loadedChannels.addAll(newLoadedChannels);
        }

        // Reload custom channels
        for (Entry<String, Channel> entry : channels.entrySet()) {
            if (loadedChannels.contains(entry.getKey())) {
                continue;
            }

            String fileName = entry.getValue().getFileName() + ".yml";

            YamlFileManager file = new YamlFileManager(customChannelDir + File.separator + fileName);
            if (file.isEmpty()) {
                String path = "channels/custom/" + fileName;
                if (plugin.getResource(path) != null) {
                    try {
                        PluginUtils.saveFile(plugin, path, customChannelDir + File.separator + fileName);
                    } catch (IOException ex) {
                        LogHelper.warning(this, "An exception occurred while trying to copy the channel file for custom channel '" + entry.getValue().getName() + "'", ex);
                        continue;
                    }
                }
            }

            if (!file.isEmpty()) {
                entry.getValue().reload(this, file.getConfig());
            }
        }

        // Load default channel
        if (defaultChannel != null && !channels.containsKey(defaultChannel.toLowerCase())) {
            LogHelper.warning(this, "The default channel '" + defaultChannel + "' is not loaded.");
        }

        firstReload = false;
    }

    private void registerPermissions(String channelName) {
        Validate.notNull(channelName, "Channel name cannot be null.");
        channelName = channelName.toLowerCase();
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.addPermission(new Permission(PermissionNodes.buildVariableNode(PermissionNodes.AUTOJOIN, channelName).getNode(), PermissionDefault.FALSE));
        pluginManager.addPermission(new Permission(PermissionNodes.buildVariableNode(PermissionNodes.JOIN, channelName).getNode(), PermissionDefault.OP));
        pluginManager.addPermission(new Permission(PermissionNodes.buildVariableNode(PermissionNodes.LEAVE, channelName).getNode(), PermissionDefault.OP));
        pluginManager.addPermission(new Permission(PermissionNodes.buildVariableNode(PermissionNodes.CHAT, channelName).getNode(), PermissionDefault.OP));
        pluginManager.addPermission(new Permission(PermissionNodes.buildVariableNode(PermissionNodes.VIEW, channelName).getNode(), PermissionDefault.TRUE));
    }

    private void unregisterPermission(String channelName) {
        Validate.notNull(channelName, "Channel name cannot be null.");
        channelName = channelName.toLowerCase();
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.removePermission(pluginManager.getPermission(PermissionNodes.buildVariableNode(PermissionNodes.AUTOJOIN, channelName).getNode()));
        pluginManager.removePermission(pluginManager.getPermission(PermissionNodes.buildVariableNode(PermissionNodes.JOIN, channelName).getNode()));
        pluginManager.removePermission(pluginManager.getPermission(PermissionNodes.buildVariableNode(PermissionNodes.LEAVE, channelName).getNode()));
        pluginManager.removePermission(pluginManager.getPermission(PermissionNodes.buildVariableNode(PermissionNodes.CHAT, channelName).getNode()));
        pluginManager.removePermission(pluginManager.getPermission(PermissionNodes.buildVariableNode(PermissionNodes.VIEW, channelName).getNode()));
    }

    public String getActiveSymbol() {
        return activeSymbol;
    }

    @Override
    public ChatFormat getGlobalFormat(String group) {
        ChatFormat format = globalFormats.get(group == null ? "default" : group);
        return format == null ? globalFormats.get("default") : format;
    }

    @Override
    public boolean registerChannel(Channel channel) {
        return registerChannel(channel, null);
    }

    @Override
    public boolean registerChannel(Channel channel, InputStream defaultConfig) {
        Validate.notNull(channel, "Channel cannot be null.");
        String name = channel.getName().toLowerCase();
        if (channels.containsKey(name)) {
            return false;
        }

        channels.put(name, channel);

        YamlFileManager file = new YamlFileManager(customChannelDir + File.separator + channel.getFileName() + ".yml");
        if (defaultConfig != null) {

            FileConfiguration config = file.getConfig();

            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfig));

            config.addDefaults(defConfig);
            config.options().copyDefaults(true);
            file.save();
        }

        file.reload();
        channel.reload(this, file.getConfig());

        LogHelper.info(this, "Registered channel '" + channel.getName() + "' (" + channel.getClass().getCanonicalName() + ")");
        return true;
    }

    @Override
    public boolean unregisterChannel(Channel channel) {
        Validate.notNull(channel, "Channel cannot be null.");
        String name = channel.getName().toLowerCase();
        if (!channels.containsKey(name)) {
            return false;
        }
        channels.put(name, channel);
        return true;
    }

    public Channel getDefaultChannel() {
        return channels.get(defaultChannel.toLowerCase());
    }

    /**
     * @return An unmodifiable collection of all loaded channels.
     */
    public Collection<Channel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    @Override
    public Channel getChannel(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return channels.get(name.toLowerCase());
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerExtendedJoinEvent e) {
        Chatter chatter = FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(e.getPlayer());
        if (chatter == null) {
            return;
        }

        // Add to autojoinable channels.
        for (Channel channel : channels.values()) {
            if (chatter.hasPermission(PermissionNodes.buildVariableNode(PermissionNodes.AUTOJOIN, channel.getName()))) {
                List<ChannelInstance> instances = channel.getInstances(chatter);
                if (instances != null && instances.size() == 1) {
                    ChannelInstance instance = instances.get(0);
                    instance.addChatter(chatter);
                }
            }
        }

        ChannelInstance active = chatter.getActiveInstance();
        if (active == null) {
            List<ChannelInstance> defaultInstances = getDefaultChannel().getInstances(chatter);

            if (defaultInstances.size() == 1) {
                ChannelInstance instance = defaultInstances.get(0);

                chatter.addInstance(instance);
                chatter.setActiveInstance(instance);
            }
        }

        active = chatter.getActiveInstance();
        if (active != null) {
            Channel channel = active.getChannel();
            e.addLoginMessage(FlexChat.class, "channel", MessageManager.getMessage(FlexChat.class, "notices.channel_active_notice", new ReplacementMap("{COLOR}", channel.getColor().toString()).put("{CHANNEL}", channel.getName()).getMap()));
        }
    }

}