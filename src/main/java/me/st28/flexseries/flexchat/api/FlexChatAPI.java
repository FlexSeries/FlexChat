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
package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexchat.api.channel.ChannelManager;
import me.st28.flexseries.flexchat.api.chatter.ChatterManager;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;

public final class FlexChatAPI {

    private FlexChatAPI() {}

    public static ChannelManager getChannelManager() {
        return FlexPlugin.getGlobalModule(ChannelManagerImpl.class);
    }

    public static ChatterManager getChatterManager() {
        return FlexPlugin.getGlobalModule(ChatterManagerImpl.class);
    }

}