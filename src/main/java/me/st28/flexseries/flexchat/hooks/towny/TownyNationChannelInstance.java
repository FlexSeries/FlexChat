package me.st28.flexseries.flexchat.hooks.towny;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.api.channel.Channel;
import me.st28.flexseries.flexchat.api.channel.ChannelInstance;

public final class TownyNationChannelInstance extends ChannelInstance {

    private Nation nation;

    public TownyNationChannelInstance(Channel channel, int uid) {
        super(channel, "nation-" + uid);

        for (Nation nation : TownyUniverse.getDataSource().getNations()) {
            if (nation.getUID() == uid) {
                this.nation = nation;
                break;
            }
        }
    }

    @Override
    public String getDisplayName() {
        return nation.getName();
    }

}