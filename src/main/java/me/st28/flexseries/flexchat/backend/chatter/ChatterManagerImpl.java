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
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.player.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.player.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;

public class ChatterManagerImpl extends FlexModule<FlexChat> implements ChatterManager, Listener, PlayerLoader {

    private final Map<String, Chatter> chatters = new HashMap<>();

    public ChatterManagerImpl(FlexChat plugin) {
        super(plugin, "chatters", "Manages chatter data", true, ChannelManagerImpl.class);
    }

    @Override
    protected void handleLoad() {
        chatters.put(ChatterConsole.NAME, new ChatterConsole());
    }

    @Override
    protected void handleSave(boolean async) {
        for (Chatter chatter : chatters.values()) {
            saveChatter(chatter);
        }
    }

    private void saveChatter(Chatter chatter) {
        YamlFileManager file = new YamlFileManager(getDataFolder() + File.separator + chatter.getIdentifier() + ".yml");

        chatter.save(file.getConfig());

        file.save();
    }

    private void loadPlayerChatter(UUID uuid) {
        String identifier = uuid.toString();
        if (chatters.containsKey(identifier)) {
            return;
        }

        ChatterPlayer chatter = new ChatterPlayer(uuid);

        YamlFileManager file = new YamlFileManager(getDataFolder() + File.separator + uuid.toString() + ".yml");
        if (file.isEmpty()) {
            LogHelper.debug(this, "Creating chatter file for '" + uuid.toString() + "'");

            Channel defaultChannel = FlexPlugin.getRegisteredModule(ChannelManagerImpl.class).getDefaultChannel();

            if (defaultChannel != null) {
                FileConfiguration config = file.getConfig();

                List<ChannelInstance> instances = defaultChannel.getInstances(chatter);
                if (instances.size() == 1) {
                    config.set("active.channel", defaultChannel.getName());
                    config.set("instances." + defaultChannel.getName(), new ArrayList<String>());
                }
            }
            file.save();
        }

        chatter.load(file.getConfig());
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
    public void loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        try {
            loadPlayerChatter(uuid);
            PlayerLoadCycle.setLoaderSuccess(cycle, this);
        } catch (Exception ex) {
            LogHelper.warning(this, "An exception occurred while loading player chatter '" + name + "'", ex);
            PlayerLoadCycle.setLoaderFailure(cycle, this);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        Chatter chatter = chatters.get(uuid.toString());
        if (chatter != null) {
            saveChatter(chatter);
            chatters.remove(uuid.toString());

            for (ChannelInstance instance : chatter.getInstances()) {
                instance.removeChatter(chatter);
            }
        }
    }

}