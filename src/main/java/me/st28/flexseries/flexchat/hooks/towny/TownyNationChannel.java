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
package me.st28.flexseries.flexchat.hooks.towny;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;

import java.util.*;

public final class TownyNationChannel extends Channel {

    final Map<Integer, ChannelInstance> instances = new HashMap<>();

    public TownyNationChannel() {
        super("Nation", "towny-nation");
    }

    public TownyNationChannelInstance getInstanceByUid(int nationUid) {
        return (TownyNationChannelInstance) instances.get(nationUid);
    }

    @Override
    public Collection<ChannelInstance> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    @Override
    public Collection<ChannelInstance> getInstances(Chatter chatter) {
        try {
            Town town = TownyUniverse.getDataSource().getResident(chatter.getName()).getTown();
            if (town == null) {
                return null;
            }

            int uid = town.getNation().getUID();

            if (!instances.containsKey(uid)) {
                instances.put(uid, new TownyNationChannelInstance(this, uid));
            }

            return Collections.singleton(instances.get(uid));
        } catch (NotRegisteredException ex) {
            return null;
        }
    }

}