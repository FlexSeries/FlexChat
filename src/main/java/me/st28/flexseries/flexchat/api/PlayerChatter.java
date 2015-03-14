package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.VanishNoPacketHook;
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
    public boolean isVisibleTo(Chatter chatter) {
        //TODO: Integrate with player options from core plugin once completed

        try {
            Player p = getPlayer();
            return p != null && !FlexPlugin.getRegisteredModule(HookManager.class).getHook(VanishNoPacketHook.class).isPlayerVanished(p);
        } catch (Exception ex) {
            return true;
        }
    }

    @Override
    public void sendMessage(MessageReference message) {
        Player p = getPlayer();
        if (p != null) {
            message.sendTo(p);
        }
    }

}