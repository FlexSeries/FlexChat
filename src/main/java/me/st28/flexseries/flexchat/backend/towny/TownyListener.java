package me.st28.flexseries.flexchat.backend.towny;

import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import me.st28.flexseries.flexchat.api.Channel;
import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TownyListener implements Listener {

    @EventHandler
    public void onTownAddResident(TownAddResidentEvent e) {
        Channel townChannel = FlexPlugin.getRegisteredModule(ChannelManager.class).getChannel(TownyTownChannel.IDENTIFIER);
        if (townChannel != null) {
            Player p = Bukkit.getPlayer(e.getResident().getName());
            if (p == null) return;

            Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(p);
            if (chatter != null) {
                chatter.addChannel(townChannel).sendMessage(p);
            }
        }
    }

    @EventHandler
    public void onTownRemoveResident(TownRemoveResidentEvent e) {
        Channel townChannel = FlexPlugin.getRegisteredModule(ChannelManager.class).getChannel(TownyTownChannel.IDENTIFIER);
        if (townChannel != null) {
            Player p = Bukkit.getPlayer(e.getResident().getName());
            if (p == null) return;

            Chatter chatter = FlexPlugin.getRegisteredModule(ChatterManager.class).getChatter(p);
            if (chatter != null && chatter.getChannels().contains(townChannel)) {
                chatter.removeChannel(townChannel).sendMessage(p);
            }
        }
    }

}