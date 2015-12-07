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
package me.st28.flexseries.flexchat.backend.chatter;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterConsole;
import me.st28.flexseries.flexchat.api.chatter.ChatterManager;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.data.DataProviderDescriptor;
import me.st28.flexseries.flexlib.player.data.PlayerDataProvider;
import me.st28.flexseries.flexlib.player.data.PlayerLoader;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatterManagerImpl extends FlexModule<FlexChat> implements ChatterManager, Listener, PlayerDataProvider {

    private final Map<String, Chatter> chatters = new HashMap<>();

    public ChatterManagerImpl(FlexChat plugin) {
        super(plugin, "chatters", "Manages chatter data", new ModuleDescriptor().setGlobal(true).setSmartLoad(false).addHardDependency(new ModuleReference("FlexChat", "channels")));
    }

    @Override
    protected void handleEnable() {
        chatters.put(ChatterConsole.NAME, new ChatterConsole());

        registerPlayerDataProvider(new DataProviderDescriptor().onlineOnly(true));
    }

    @Override
    protected void handleSave(boolean async) {
        for (Chatter chatter : chatters.values()) {
            saveChatter(chatter);
        }
    }

    private void saveChatter(Chatter chatter) {
        if (chatter instanceof ChatterPlayer) {
            chatter.save(FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(((ChatterPlayer) chatter).getUuid()).getCustomSection(FlexChat.class));
            return;
        }

        YamlFileManager file = new YamlFileManager(getDataFolder() + File.separator + chatter.getIdentifier() + ".yml");

        chatter.save(file.getConfig());

        file.save();
    }

    private void loadPlayerChatter(UUID uuid, PlayerData data) {
        String identifier = uuid.toString();
        if (chatters.containsKey(identifier)) {
            return;
        }

        ChatterPlayer chatter = new ChatterPlayer(uuid);

        Channel defaultChannel = FlexPlugin.getGlobalModule(ChannelManagerImpl.class).getDefaultChannel();

        ConfigurationSection config = data.getCustomSection(FlexChat.class);
        if (defaultChannel != null) {
            Collection<ChannelInstance> instances = defaultChannel.getInstances(chatter);
            if (instances.size() == 1) {
                config.set("active.channel", defaultChannel.getName());
                config.set("instances." + defaultChannel.getName(), new ArrayList<String>());

                data.setCustomData(FlexChat.class, "active.channel", defaultChannel.getName());
                data.setCustomData(FlexChat.class, "instances." + defaultChannel.getName(), new ArrayList<String>());
            }
        }

        chatter.load(config);
        chatters.put(identifier, chatter);
    }

    public Collection<Chatter> getChatters() {
        return Collections.unmodifiableCollection(chatters.values());
    }

    @Override
    public Chatter getChatter(CommandSender sender) {
        if (sender instanceof Player) {
            return chatters.get(((Player) sender).getUniqueId().toString());
        }
        return chatters.get(ChatterConsole.NAME);
    }

    @Override
    public Chatter getChatter(String identifier) {
        return chatters.get(identifier);
    }

    @Override
    public void loadPlayer(PlayerLoader loader, PlayerData data, PlayerReference player) {
        loadPlayerChatter(player.getUuid(), data);
    }

    @Override
    public void savePlayer(PlayerLoader loader, PlayerData data, PlayerReference player) {
        Chatter chatter = chatters.get(player.getUuid().toString());
        if (chatter != null) {
            saveChatter(chatter);
        }
    }

    @Override
    public boolean unloadPlayer(PlayerLoader loader, PlayerData data, PlayerReference player, boolean force) {
        Chatter chatter = chatters.remove(player.getUuid().toString());
        if (chatter != null) {
            for (ChannelInstance instance : chatter.getInstances()) {
                instance.removeOfflineChatter(chatter);
            }
        }
        return true;
    }

}