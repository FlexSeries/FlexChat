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
package me.st28.flexseries.flexchat.api.chatter;

import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatterPlayer extends Chatter {

    private UUID uuid;

    public ChatterPlayer(UUID uuid) {
        super(uuid.toString());
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public String getName() {
        return getPlayer().getName();
    }

    @Override
    public String getDisplayName() {
        String name = getPlayer().getDisplayName();
        return name == null ? getName() : name;
    }

    @Override
    public boolean hasPermission(PermissionNode permission) {
        return permission.isAllowed(getPlayer());
    }

    @Override
    public void sendMessage(String message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.sendMessage(message);
    }

    @Override
    public void sendMessage(MessageReference message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        message.sendTo(player);
    }

}