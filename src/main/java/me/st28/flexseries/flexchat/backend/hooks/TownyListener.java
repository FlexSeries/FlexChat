package me.st28.flexseries.flexchat.backend.hooks;

import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TownyListener implements Listener {

    @EventHandler
    public void onTownAddResident(TownAddResidentEvent e) {
        FlexPlugin.getRegisteredModule(ChannelManager.class).
    }

    @EventHandler
    public void onTownRemoveResident(TownRemoveResidentEvent e) {

    }

}