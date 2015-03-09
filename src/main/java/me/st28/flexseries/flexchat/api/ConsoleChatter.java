package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.messages.MessageReference;
import org.bukkit.Bukkit;

/**
 * Represents the console as a chatter.
 */
public final class ConsoleChatter extends Chatter {

    public final static String IDENTIFIER = "CONSOLE";

    public ConsoleChatter() {
        super(IDENTIFIER);
    }

    @Override
    public String getName() {
        return IDENTIFIER;
    }

    @Override
    public void sendMessage(MessageReference message) {
        message.sendTo(Bukkit.getConsoleSender());
    }

}