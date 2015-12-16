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

import com.gmail.nossr50.datatypes.party.Party;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;

public final class McMmoPartyChannelInstance extends ChannelInstance {

    private Party party;

    public McMmoPartyChannelInstance(Channel channel, Party party) {
        super(channel, "party-" + party.getName());

        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    @Override
    public String getDisplayName() {
        return party.getName();
    }

}