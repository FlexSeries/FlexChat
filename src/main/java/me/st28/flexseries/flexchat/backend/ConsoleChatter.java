package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.api.Chatter;
import org.bukkit.Bukkit;

/**
 * Represents the console as a chatter.
 */
public final class ConsoleChatter extends Chatter {

    public final static String NAME = "CONSOLE";

    public ConsoleChatter() {
        super(NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void sendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

}