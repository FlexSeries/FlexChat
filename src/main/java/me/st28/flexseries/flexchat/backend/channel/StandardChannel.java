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
package me.st28.flexseries.flexchat.backend.channel;

import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents a channel loaded by FlexChat.
 */
public class StandardChannel extends Channel {

    private ChannelInstance instance;

    public StandardChannel(String name) {
        super(name);
        instance = new ChannelInstance(this, null);
    }

    @Override
    public Collection<ChannelInstance> getInstances() {
        return Collections.singletonList(instance);
    }

    @Override
    public Collection<ChannelInstance> getInstances(Chatter chatter) {
        return Collections.singleton(instance);
    }

}