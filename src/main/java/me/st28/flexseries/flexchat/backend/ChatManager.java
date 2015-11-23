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

        Player player = e.getPlayer();

        Chatter chatter = FlexChatAPI.getChatterManager().getChatter(player);
        if (chatter == null) {
            MessageManager.getMessage(FlexChat.class, "errors.unable_to_chat").sendTo(player);
            return;
        }

        ChannelInstance active = chatter.getActiveInstance();
        if (active == null) {
            MessageManager.getMessage(FlexChat.class, "errors.channel_active_not_set").sendTo(player);
            return;
        }

        Collection<Chatter> recipients = active.getApplicableChatters(chatter);

        Iterator<Chatter> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            Chatter oChatter = iterator.next();

            PlayerData data = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(((ChatterPlayer) oChatter).getUuid());
            List<String> ignored = data.getCustomData("ignored", List.class);
            if (ignored != null && !chatter.hasPermission(PermissionNodes.BYPASS_IGNORE) && ignored.contains(chatter.getIdentifier())) {
                iterator.remove();
            }
        }

        String sendMessage = e.getFormat().replace("{MESSAGE}", ChatFormat.applyApplicableChatColors(chatter, e.getMessage()));

        for (Chatter oChatter : recipients) {
            oChatter.sendMessage(sendMessage);
        }

        Bukkit.getPluginManager().callEvent(new ChannelChatEvent(active, chatter, recipients, e.getMessage()));

        ChatLogHelper.log(active, ChatColor.stripColor(e.getFormat().replace("{MESSAGE}", e.getMessage())));
    }

}