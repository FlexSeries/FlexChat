/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexchat.hooks.mcmmo;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class McMmoListener implements Listener {

    private McMmoPartyChannel channel;

    public McMmoListener(McMmoPartyChannel channel) {
        this.channel = channel;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPartyChange(McMMOPartyChangeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        String newName = e.getNewParty();
        String oldName = e.getOldParty();

        if (newName == null) {
            // Party was removed, disbanded, etc. Remove entirely

            channel.instances.remove(e.getOldParty().toLowerCase()).removeAllChatters();
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(FlexChat.class), () -> {
                Party party = null;

                for (Party curParty : PartyAPI.getParties()) {
                    if (curParty.getName().equalsIgnoreCase(newName)) {
                        party = curParty;
                    }
                }

                if (party == null) {
                    return;
                }

                Chatter chatter = FlexChatAPI.getChatterManager().getChatter(uuid.toString());
                if (chatter == null) {
                    return;
                }

                McMmoPartyChannelInstance instance = channel.getPartyInstance(party);
                if (instance != null && instance.addChatter(chatter)) {
                    instance.alertJoin(chatter);
                }

                // Remove old channel instance
                if (oldName != null) {
                    McMmoPartyChannelInstance oldInstance = channel.instances.get(oldName.toLowerCase());

                    if (oldInstance.getChatters().isEmpty()) {
                        channel.instances.remove(oldName.toLowerCase());
                    }
                }
            });
        }
    }

}