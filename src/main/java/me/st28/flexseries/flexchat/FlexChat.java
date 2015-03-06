package me.st28.flexseries.flexchat;

import me.st28.flexseries.flexchat.api.ChannelManager;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.commands.CmdChannel;
import me.st28.flexseries.flexcore.commands.FlexCommandWrapper;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;

public final class FlexChat extends FlexPlugin {

    @Override
    public void handlePluginLoad() {
        registerModule(new ChannelManager(this));
        registerModule(new ChatterManager(this));
    }

    @Override
    public void handlePluginEnable() {
        FlexCommandWrapper.registerCommand(this, "flexchannel", new CmdChannel(this));
    }

}