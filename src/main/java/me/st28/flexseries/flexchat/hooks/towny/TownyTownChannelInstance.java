package me.st28.flexseries.flexchat.hooks.towny;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;

public final class TownyTownChannelInstance extends ChannelInstance {

    private Town town;

    public TownyTownChannelInstance(Channel channel, int uid) {
        super(channel, "town-" + uid);

        for (Town town : TownyUniverse.getDataSource().getTowns()) {
            if (town.getUID() == uid) {
                this.town = town;
                break;
            }
        }
    }

    @Override
    public String getDisplayName() {
        return town.getName();
    }

}