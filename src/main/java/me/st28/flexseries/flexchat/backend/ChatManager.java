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
package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChannelChatEvent;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.backend.channel.ChannelManagerImpl;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public final class ChatManager extends FlexModule<FlexChat> implements Listener {

    public ChatManager(FlexChat plugin) {
        super(plugin, "chat", "Manages chat", false, ChannelManagerImpl.class, ChatterManagerImpl.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatLowest(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Chatter chatter = FlexChatAPI.getChatterManager().getChatter(player);
        if (chatter == null) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        ChannelInstance active = chatter.getActiveInstance();
        if (active == null) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.channel_active_not_set").sendTo(player);
            return;
        }

        if (!chatter.hasPermission(PermissionNodes.buildVariableNode(PermissionNodes.CHAT, active.getChannel().getName()))) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "chat in").put("{CHANNEL}", active.getChannel().getName()).getMap()).sendTo(player);
            return;
        }

        ChatFormat chatFormat = active.getChannel().getChatFormat(chatter);
        if (chatFormat == null) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        e.setFormat(chatFormat.getFormattedResult(chatter, active.getChannel(), e.getMessage()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatHighest(AsyncPlayerChatEvent e) {
        e.setCancelled(true); // FlexChat will handle sending the message.

        Player player = e.getPlayer();

        Chatter chatter = FlexChatAPI.getChatterManager().getChatter(player);
        if (chatter == null) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        ChannelInstance active = chatter.getActiveInstance();
        if (active == null) {
            e.setCancelled(true);
            MessageReference.create(FlexChat.class, "errors.channel_active_not_set").sendTo(player);
            return;
        }

        int radius = active.getChannel().getRadius();

        Set<Chatter> recipients = new HashSet<>();

        if (radius <= 0) {
            recipients.addAll(active.getChatters());
        } else {
            Location senderLoc = ((ChatterPlayer) chatter).getPlayer().getLocation();
            radius = (int) Math.pow(radius, 2D);

            for (Chatter oChatter : active.getChatters()) {
                if (oChatter instanceof ChatterPlayer) {
                    if (((ChatterPlayer) oChatter).getPlayer().getLocation().distanceSquared(senderLoc) > radius) {
                        continue;
                    }
                }

                recipients.add(oChatter);
            }
        }

        for (Chatter oChatter : recipients) {
            oChatter.sendMessage(e.getFormat());
        }

        Bukkit.getPluginManager().callEvent(new ChannelChatEvent(active, chatter, recipients, e.getMessage()));
    }

}