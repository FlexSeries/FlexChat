package me.st28.flexseries.flexchat.commands.chat_spy;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CmdChatSpy extends FlexCommand<FlexChat> {

    public CmdChatSpy(FlexChat plugin) {
        super(
                plugin,
                "flexchatspy",
                new FlexCommandSettings<FlexChat>()
                    .defaultSubcommand("toggle")
                    .setDummyCommand()
        );

        registerSubcommand(new SCmdChatSpyToggle(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {

    }

}