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
package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.ChannelChatEvent;
import me.st28.flexseries.flexchat.api.FlexChatAPI;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;
import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.api.chatter.ChatterPlayer;
import me.st28.flexseries.flexchat.api.format.ChatFormat;
import me.st28.flexseries.flexchat.logging.ChatLogHelper;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class ChatManager extends FlexModule<FlexChat> implements Listener {

    public ChatManager(FlexChat plugin) {
        super(
                plugin,
                "chat",
                "Manages chat",
                new ModuleDescriptor()
                        .setGlobal(true)
                        .setSmartLoad(false)
                        .addHardDependency(new ModuleReference("FlexChat", "channels"))
                        .addHardDependency(new ModuleReference("FlexChat", "chatters"))
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatLowest(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Chatter chatter = FlexChatAPI.getChatterManager().getChatter(player);
        if (chatter == null) {
            e.setCancelled(true);
            MessageManager.getMessage(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        ChannelInstance active = chatter.getActiveInstance();
        if (active == null) {
            e.setCancelled(true);
            MessageManager.getMessage(FlexChat.class, "errors.channel_active_not_set").sendTo(player);
            return;
        }

        if (!chatter.hasPermission(PermissionNode.buildVariableNode(PermissionNodes.CHAT, active.getChannel().getName()))) {
            e.setCancelled(true);
            MessageManager.getMessage(FlexChat.class, "errors.channel_no_permission", new ReplacementMap("{VERB}", "chat in").put("{CHANNEL}", active.getChannel().getName()).getMap()).sendTo(player);
            return;
        }

        ChatFormat chatFormat = active.getChannel().getChatFormat(chatter);
        if (chatFormat == null) {
            e.setCancelled(true);
            MessageManager.getMessage(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        e.setFormat(chatFormat.getFormattedResult(chatter, active.getChannel()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatHighest(AsyncPlayerChatEvent e) {
        e.setCancelled(true); // FlexChat will handle sending the message.

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Player player = e.getPlayer();

            // Get the chatter. If null, send an error message.
            final Chatter chatter = FlexChatAPI.getChatterManager().getChatter(player);
            if (chatter == null) {
                MessageManager.getMessage(FlexChat.class, "errors.unable_to_chat").sendTo(player);
                return;
            }

            // Get the active instance. If null, send an error message.
            final ChannelInstance active = chatter.getActiveInstance();
            if (active == null) {
                MessageManager.getMessage(FlexChat.class, "errors.channel_active_not_set").sendTo(player);
                return;
            }

            // Check if the channel is muted
            if (FlexChatAPI.getChannelManager().isChannelMuted(active.getChannel()) && !chatter.hasPermission(PermissionNodes.BYPASS_MUTE)) {
                MessageManager.getMessage(FlexChat.class, "errors.unable_to_chat_muted", new ReplacementMap("{CHANNEL}", active.getChannel().getName()).getMap()).sendTo(player);
                return;
            }

            // Determine who the recipients are
            final Collection<Chatter> recipients = active.getApplicableChatters(chatter);

            if (!chatter.hasPermission(PermissionNodes.BYPASS_IGNORE)) {
                // Only look for ignore exclusions if the chatter cannot bypass ignores.

                PlayerManager playerManager = FlexPlugin.getGlobalModule(PlayerManager.class);
                for (Iterator<Chatter> it = recipients.iterator(); it.hasNext(); ) {
                    Chatter oChatter = it.next();

                    PlayerData data = playerManager.getPlayerData(((ChatterPlayer) oChatter).getUuid());
                    List<String> ignored = data.getCustomData("ignored", List.class);
                    if (ignored != null && ignored.contains(chatter.getIdentifier())) {
                        // If the other player has ignored the sender, remove them from the recipients
                        it.remove();
                    }
                }
            }

            // Send the message, call new event, and log to the console.
            String sendMessage = e.getFormat().replace("{MESSAGE}", ChatFormat.applyApplicableChatColors(chatter, e.getMessage()));

            for (Chatter oChatter : recipients) {
                oChatter.sendMessage(sendMessage);
            }

            Bukkit.getPluginManager().callEvent(new ChannelChatEvent(active, chatter, recipients, e.getMessage()));

            ChatLogHelper.log(active, ChatColor.stripColor(e.getFormat().replace("{MESSAGE}", e.getMessage())));
        });
    }

}