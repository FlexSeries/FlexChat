package me.st28.flexseries.flexchat.backend.hooks;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.backend.ChannelManager;
import me.st28.flexseries.flexchat.backend.FileChannel;
import me.st28.flexseries.flexchat.backend.PlayerChatter;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class TownyTownChannel extends FileChannel {

    public TownyTownChannel(String shortName, ChatColor color, Collection<String> banned, Map<String, String> formats) {
        super("town", shortName, color, banned, formats);
    }

    @Override
    public Collection<Chatter> getRecipients(Chatter target) {
        return getChatters(target);
    }

    @Override
    public Collection<Chatter> getChatters(Chatter target) {
        if (!(target instanceof PlayerChatter)) {
            return new ArrayList<>();
        }

        try {
            List<Chatter> returnList = new ArrayList<>();

            ChannelManager channelManager = FlexPlugin.getRegisteredModule(ChannelManager.class);
            for (Player player : TownyUniverse.getOnlinePlayers(TownyUniverse.getDataSource().getResident(target.getName()).getTown())) {
                returnList.add(channelManager.getChatter(player));
            }

            return returnList;
        } catch (NotRegisteredException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public DynamicResponse canChatterJoin(Chatter chatter) {
        if (!(chatter instanceof PlayerChatter)) {
            return new DynamicResponse(false, MessageReference.createGeneral(FlexChat.class, "errors.must_be_player"));
        }

        try {
            TownyUniverse.getDataSource().getResident(chatter.getName()).getTown();
            return new DynamicResponse(true);
        } catch (NotRegisteredException ex) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "hooks.towny.errors.not_in_town"));
        }
    }

}