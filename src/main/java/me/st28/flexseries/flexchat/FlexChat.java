package me.st28.flexseries.flexchat;

import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.backend.ChatAdminManager;
import me.st28.flexseries.flexchat.commands.CmdMessage;
import me.st28.flexseries.flexchat.commands.CmdReply;
import me.st28.flexseries.flexchat.commands.channel.CmdChannel;
import me.st28.flexseries.flexchat.commands.chat_spy.CmdChatSpy;
import me.st28.flexseries.flexcore.commands.FlexCommandWrapper;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FlexChat extends FlexPlugin {

    public final static Logger CHAT_LOGGER = LogManager.getLogger(ChannelManager.class.getCanonicalName());

    @Override
    public void handlePluginLoad() {
        registerModule(new ChannelManager(this));
        registerModule(new ChatterManager(this));
        registerModule(new ChatAdminManager(this));
    }

    @Override
    public void handlePluginEnable() {
        FlexCommandWrapper.registerCommand(this, "flexchannel", new CmdChannel(this));
        FlexCommandWrapper.registerCommand(this, "flexchatspy", new CmdChatSpy(this));

        CmdMessage messageCommand = new CmdMessage(this);
        FlexCommandWrapper.registerCommand(this, "flexmessage", messageCommand);
        FlexCommandWrapper.registerCommand(this, "flexreply", new CmdReply(this, messageCommand));
    }

}