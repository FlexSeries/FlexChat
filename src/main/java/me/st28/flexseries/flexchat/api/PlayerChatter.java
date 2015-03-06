package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.players.PlayerUUIDTracker;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a player as a chatter.
 */
public final class PlayerChatter extends Chatter {

    private final UUID uuid;

    public PlayerChatter(UUID uuid) {
        super(uuid.toString());

        this.uuid = uuid;
    }

    public final Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public String getName() {
        return FlexPlugin.getRegisteredModule(PlayerUUIDTracker.class).getName(uuid);
    }

    @Override
    public String getDisplayName() {
        return FlexPlugin.getRegisteredModule(PlayerUUIDTracker.class).getTopLevelName(uuid);
    }

    @Override
    public void sendMessage(MessageReference message) {
        Player p = getPlayer();
        if (p != null) {
            message.sendTo(p);
        }
    }

}