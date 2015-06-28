package me.st28.flexseries.flexchat.hooks.towny;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class TownyListener implements Listener {

    private TownyTownChannel townChannel;
    private TownyNationChannel nationChannel;

    public TownyListener(TownyTownChannel townChannel, TownyNationChannel nationChannel) {
        this.townChannel = townChannel;
        this.nationChannel = nationChannel;
    }

    @EventHandler
    public void onTownDelete(DeleteTownEvent e) {
        List<Integer> uids = TownyUniverse.getDataSource().getTowns().stream().map(Town::getUID).collect(Collectors.toList());

        Iterator<Entry<Integer, ChannelInstance>> iterator = townChannel.instances.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, ChannelInstance> next = iterator.next();
            if (!uids.contains(next.getKey())) {
                iterator.remove();

                next.getValue().removeAllChatters();
                next.getValue().removeAllSpies();
            }
        }
    }

    @EventHandler
    public void onNationDelete(DeleteNationEvent e) {
        List<Integer> uids = TownyUniverse.getDataSource().getNations().stream().map(Nation::getUID).collect(Collectors.toList());

        Iterator<Entry<Integer, ChannelInstance>> iterator = nationChannel.instances.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, ChannelInstance> next = iterator.next();
            if (!uids.contains(next.getKey())) {
                iterator.remove();

                next.getValue().removeAllChatters();
                next.getValue().removeAllSpies();
            }
        }
    }

}