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
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class McMmoPartyChannel extends Channel {

    final Map<String, McMmoPartyChannelInstance> instances = new HashMap<>();

    public McMmoPartyChannel() {
        super("Party", "mcmmo-party");
    }

    @Override
    public Collection<ChannelInstance> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public McMmoPartyChannelInstance getPartyInstance(Party party) {
        String name = party.getName().toLowerCase();

        if (!instances.containsKey(name.toLowerCase())) {
            instances.put(name, new McMmoPartyChannelInstance(this, party));
        }

        return instances.get(name);
    }

    @Override
    public Collection<ChannelInstance> getInstances(Chatter chatter) {
        try {
            if (!(chatter instanceof ChatterPlayer)) {
                return null;
            }

            Player player = Bukkit.getPlayer(((ChatterPlayer) chatter).getUuid());
            if (player == null) {
                return null;
            }

            String ptName = PartyAPI.getPartyName(player);

            for (Party party : PartyAPI.getParties()) {
                if (party.getName().equalsIgnoreCase(ptName)) {
                    return Collections.singleton(getPartyInstance(party));
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

}