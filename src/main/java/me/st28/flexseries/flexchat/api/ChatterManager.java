package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChatterManager extends FlexModule<FlexChat> implements Listener, PlayerLoader {

    private final Map<String, Chatter> chatters = new HashMap<>();

    File chatterDir;

    public ChatterManager(FlexChat plugin) {
        super(plugin, "chatters", "Manages chatters", false, ChannelManager.class, PlayerManager.class);
    }

    @Override
    protected void handleLoad() throws Exception {
        chatterDir = new File(plugin.getDataFolder() + File.separator + "chatters");

        loadChatterData(new ConsoleChatter());
    }

    @Override
    protected void handleReload() {
        chatterDir.mkdirs();
    }

    @Override
    protected void handleSave(boolean async) {
        for (Chatter chatter : chatters.values()) {
            chatter.data.save();
        }
    }

    public void refreshChatters() {
        for (Chatter chatter : chatters.values()) {
            chatter.data.refreshChannels();
        }
    }

    public void loadChatterData(Chatter chatter) {
        String identifier = chatter.getIdentifier();
        if (chatters.containsKey(identifier)) {
            return;
        }

        YamlFileManager file = new YamlFileManager(chatterDir + File.separator + identifier + ".yml");
        FileConfiguration config = file.getConfig();

        ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);

        if (file.isEmpty()) {
            // Create data
            LogHelper.info(this, "Creating chatter file for '" + identifier + "' (" + chatter.getName() + ")");
            Channel defChannel = channelManager.getDefaultChannel();
            if (defChannel != null) {
                String defIdentifier = defChannel.getIdentifier();

                if (defChannel.addChatter(chatter, true).isSuccess()) {
                    config.set("activeChannel", defIdentifier);
                    config.set("channels." + defIdentifier, System.currentTimeMillis());
                }
            }
            file.save();
        }

        chatter.data = new ChatterData(file);
        chatters.put(identifier, chatter);
        chatter.data.refreshChannels();

        for (Channel channel : chatter.data.getChannels()) {
            if (!channel.getChatters(chatter).contains(chatter) && !channel.addChatter(chatter, true).isSuccess()) {
                chatter.data.channels.remove(channel.getIdentifier());
            }
        }

        Channel activeChannel = chatter.getActiveChannel();
        if (activeChannel != null && !chatter.getChannels().contains(activeChannel)) {
            chatter.setActiveChannel(chatter.data.getNextChannel());
        }
    }

    /**
     * @return the {@link me.st28.flexseries.flexchat.api.Chatter} instance of a given CommandSender.
     */
    public Chatter getChatter(CommandSender sender) {
        Validate.notNull(sender, "Sender cannot be null.");

        if (sender instanceof Player) {
            return chatters.get(((Player) sender).getUniqueId().toString());
        }

        return chatters.get(ConsoleChatter.IDENTIFIER);
    }

    @Override
    public boolean isPlayerLoadSync() {
        return false;
    }

    @Override
    public boolean loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        if (!cycle.isLoaderComplete(PlayerManager.class)) return false;

        loadChatterData(new PlayerChatter(uuid));
        PlayerLoadCycle.completedCycle(cycle, this);
        return true;
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerJoinLoadedEvent e) {
        Chatter chatter = getChatter(e.getPlayer());

        Channel channel = chatter.getActiveChannel();

        if (channel != null) {
            e.addLoginMessage(FlexChat.class, "channel", MessageReference.create(FlexChat.class, "notices.login_channel", new ReplacementMap("{CHANNEL}", channel.getName()).put("{COLOR}", channel.getColor().toString()).getMap()));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent e) {
        String identifier = e.getPlayer().getUniqueId().toString();
        Chatter chatter = chatters.get(identifier);
        if (chatter == null) return;

        chatter.data.save();
        for (Channel channel : chatter.getChannels()) {
            channel.removeChatter(chatter, true);
        }
        chatters.remove(identifier);
    }

}