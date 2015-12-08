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

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;

public final class TownyNationChannelInstance extends ChannelInstance {

    private Nation nation;

    public TownyNationChannelInstance(Channel channel, int uid) {
        super(channel, "nation-" + uid);

        for (Nation nation : TownyUniverse.getDataSource().getNations()) {
            if (nation.getUID() == uid) {
                this.nation = nation;
                break;
            }
        }
    }

    @Override
    public String getDisplayName() {
        return nation.getName();
    }

}