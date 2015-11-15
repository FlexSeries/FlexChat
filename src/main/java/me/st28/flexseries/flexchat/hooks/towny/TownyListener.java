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
package me.st28.flexseries.flexchat.hooks.towny;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterManager;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
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

    private Chatter getChatter(Resident resident) {
        return FlexPlugin.getGlobalModule(ChatterManagerImpl.class).getChatter(Bukkit.getPlayer(resident.getName()));
    }

    private void addToInstances(Chatter chatter, Channel channel) {
        Collection<ChannelInstance> instances = channel.getInstances(chatter);
        if (chatter == null || instances == null || instances.isEmpty()) {
            return;
        }

        for (ChannelInstance instance : instances) {
            if (instance.addChatter(chatter)) {
                instance.alertJoin(chatter);
            }
        }
    }

    @EventHandler
    public void onTownAddResident(TownAddResidentEvent e) {
        addToInstances(getChatter(e.getResident()), townChannel);
    }

    @EventHandler
    public void onTownRemoveResident(TownRemoveResidentEvent e) {
        Chatter chatter = getChatter(e.getResident());
        if (chatter == null) {
            return;
        }

        TownyTownChannelInstance instance = townChannel.getInstanceByUid(e.getTown().getUID());
        if (instance != null) {
            if (instance.removeChatter(chatter)) {
                instance.alertLeave(chatter);
            }
        }
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
            }
        }
    }

    @EventHandler
    public void onNationAddTown(NationAddTownEvent e) {
        for (Resident resident : e.getTown().getResidents()) {
            addToInstances(getChatter(resident), nationChannel);
        }
    }

    @EventHandler
    public void onNationRemoveTown(NationRemoveTownEvent e) {
        TownyNationChannelInstance instance = nationChannel.getInstanceByUid(e.getNation().getUID());
        if (instance != null) {
            instance.removeAllChatters();
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
            }
        }
    }

}