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

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;

public final class TownyTownChannelInstance extends ChannelInstance {

    private Town town;

    public TownyTownChannelInstance(Channel channel, int uid) {
        super(channel, "town-" + uid);

        for (Town town : TownyUniverse.getDataSource().getTowns()) {
            if (town.getUID() == uid) {
                this.town = town;
                break;
            }
        }

        if (town == null) {
            throw new IllegalStateException("Town with UID " + uid + " not found.");
        }
    }

    public Town getTown() {
        return town;
    }

    @Override
    public String getDisplayName() {
        return town.getName();
    }

}