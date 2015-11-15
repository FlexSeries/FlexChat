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