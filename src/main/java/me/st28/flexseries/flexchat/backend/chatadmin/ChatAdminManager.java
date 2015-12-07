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
package me.st28.flexseries.flexchat.backend.chatadmin;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChannelChatEvent;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerExtendedJoinEvent;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.data.DataProviderDescriptor;
import me.st28.flexseries.flexlib.player.data.PlayerDataProvider;
import me.st28.flexseries.flexlib.player.data.PlayerLoader;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatAdminManager extends FlexModule<FlexChat> implements Listener, PlayerDataProvider {

    public final static Pattern COMMAND_LABEL_PATTERN = Pattern.compile("(?i)^(/[^ ]+).+");

    private String commandOutput;
    private final List<String> spyCommands = new ArrayList<>();

    private String channelOutput;
    private String instanceOutput;

    private Map<UUID, SpySettings> spies = new HashMap<>();
    private YamlFileManager spyFile;

    public ChatAdminManager(FlexChat plugin) {
        super(
                plugin,
                "chat_admin",
                "Manages chat administration features",
                new ModuleDescriptor()
                        .setGlobal(true)
                        .setSmartLoad(false)
                        .addHardDependency(new ModuleReference("FlexChat", "channels"))
        );
    }

    @Override
    protected void handleEnable() {
        spyFile = new YamlFileManager(getDataFolder() + File.separator + "chatSpies.yml");

        registerPlayerDataProvider(new DataProviderDescriptor());
    }

    private void loadPlayer(UUID uuid) {
        FileConfiguration config = spyFile.getConfig();

        spies.put(uuid, new SpySettings(config.getConfigurationSection(uuid.toString())));
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        commandOutput = StringEscapeUtils.unescapeJava(config.getString("command spy.format", "&c[&7SPY&c] &7{SENDER}&7: &f{COMMAND}"));
        channelOutput = StringEscapeUtils.unescapeJava(config.getString("channel spy.channel format", "&4[&cSPY&4] &8[&7{CHANNEL}&8] &7{SENDER}&7: &f{MESSAGE}"));
        instanceOutput = StringEscapeUtils.unescapeJava(config.getString("channel spy.instance format", "&4[&cSPY&4] &8[&7{CHANNEL}&8:&7{INSTANCE}&8] &7{SENDER}&7: &f{MESSAGE}"));

        spyCommands.clear();
        spyCommands.addAll(config.getStringList("command spy.commands").stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    @Override
    protected void handleSave(boolean async) {
        FileConfiguration config = spyFile.getConfig();

        for (Entry<UUID, SpySettings> entry : spies.entrySet()) {
            ConfigurationSection section = config.createSection(entry.getKey().toString());

            entry.getValue().save(section);
        }

        spyFile.save();
    }

    public boolean isSpyEnabled(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        if (!spies.containsKey(uuid)) {
            return false;
        }

        return spies.get(uuid).isEnabled();
    }

    public SpySettings getSpySettings(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");

        if (!spies.containsKey(uuid)) {
            spies.put(uuid, new SpySettings());
        }

        return spies.get(uuid);
    }

    private List<Player> getOnlineSpies() {
        List<Player> list = new ArrayList<>();

        for (UUID uuid : spies.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                list.add(player);
            }
        }

        return list;
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerExtendedJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!PermissionNodes.SPY_PERSISTENT.isAllowed(e.getPlayer())) {
            spies.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();
        Matcher matcher = COMMAND_LABEL_PATTERN.matcher(message);
        if (matcher.matches()) {
            if (spyCommands.contains(matcher.group(1).toLowerCase())) {
                String sendMessage = ChatColor.translateAlternateColorCodes('&', commandOutput.replace("{SENDER}", e.getPlayer().getName())).replace("{COMMAND}", message);

                for (Player spy : getOnlineSpies()) {
                    if (isSpyEnabled(spy.getUniqueId())) {
                        spy.sendMessage(sendMessage);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChannelChat(ChannelChatEvent e) {
        String sendMessage;

        ChannelInstance instance = e.getChannelInstance();
        if (instance.getLabel() == null) {
            sendMessage = channelOutput;
        } else {
            sendMessage = instanceOutput.replace("{INSTANCE}", instance.getDisplayName());
        }

        sendMessage = ChatColor.translateAlternateColorCodes('&', sendMessage.replace("{SENDER}", e.getSender().getName()).replace("{CHANNEL}", instance.getChannel().getName())).replace("{MESSAGE}", e.getMessage());

        ChatterManagerImpl chatterManager = FlexPlugin.getGlobalModule(ChatterManagerImpl.class);

        PermissionNode permission = PermissionNode.buildVariableNode(PermissionNodes.SPY, instance.getChannel().getName().toLowerCase());
        for (Player spy : getOnlineSpies()) {
            if (isSpyEnabled(spy.getUniqueId())
                    && getSpySettings(spy.getUniqueId()).containsInstance(instance)
                    && permission.isAllowed(spy)) {
                Chatter chatter = chatterManager.getChatter(spy);
                if (!instance.containsChatter(chatter) || !instance.getApplicableChatters(chatter).contains(e.getSender())) {
                    // Only send if the chatter is not in the instance or the applicable chatters
                    // doesn't contain the sender (usually only happens with range-based channels)
                    spy.sendMessage(sendMessage);
                }
            }
        }
    }

    @Override
    public void loadPlayer(PlayerLoader loader, PlayerData data, PlayerReference player) {
        loadPlayer(player.getUuid());
    }

}