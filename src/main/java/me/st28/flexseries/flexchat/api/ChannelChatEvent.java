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

import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public final class ChannelChatEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private ChannelInstance instance;
    private Chatter sender;
    private Collection<Chatter> recipients;

    private String message;

    public ChannelChatEvent(ChannelInstance instance, Chatter sender, Collection<Chatter> recipients, String message) {
        this.instance = instance;
        this.sender = sender;
        this.recipients = recipients;
        this.message = message;
    }

    public ChannelInstance getChannelInstance() {
        return instance;
    }

    public Chatter getSender() {
        return sender;
    }

    public Collection<Chatter> getRecipients() {
        return recipients;
    }

    public String getMessage() {
        return message;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}