package me.st28.flexseries.flexchat.backend.towny;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.api.Chatter;
import me.st28.flexseries.flexchat.api.ChatterManager;
import me.st28.flexseries.flexchat.api.PlayerChatter;
import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.DynamicResponse;
import org.bukkit.entity.Player;

import java.util.*;

public final class TownyNationChannel extends TrackingConfigurableChannel {

    public final static String IDENTIFIER = "Towny-nation";

    private Set<Chatter> chatters = new HashSet<>();

    public TownyNationChannel() {
        super(IDENTIFIER);
    }

    @Override
    public boolean isJoinableByCommand() {
        return false;
    }

    @Override
    public boolean isLeaveableByCommand() {
        return false;
    }

    @Override
    public Collection<Chatter> getChatters(Chatter target) {
        if (!(target instanceof PlayerChatter)) {
            return new ArrayList<>();
        }

        try {
            List<Chatter> returnList = new ArrayList<>();

            ChatterManager chatterManager = FlexPlugin.getRegisteredModule(ChatterManager.class);
            for (Player player : TownyUniverse.getOnlinePlayers(TownyUniverse.getDataSource().getResident(target.getName()).getTown().getNation())) {
                returnList.add(chatterManager.getChatter(player));
            }

            return returnList;
        } catch (NotRegisteredException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public DynamicResponse canChatterJoin(Chatter chatter) {
        if (!(chatter instanceof PlayerChatter)) {
            return new DynamicResponse(false, MessageReference.createGeneral(FlexCore.class, "general.errors.must_be_player"));
        }

        DynamicResponse superResponse = super.canChatterJoin(chatter);
        if (!superResponse.isSuccess()) {
            return superResponse;
        }

        try {
            Town town = TownyUniverse.getDataSource().getResident(chatter.getName()).getTown();

            try {
                town.getNation();
            } catch (NotRegisteredException ex) {
                return new DynamicResponse(false, MessageReference.create(FlexChat.class, "hooks.towny.errors.not_in_nation"));
            }
            return new DynamicResponse(true);
        } catch (NotRegisteredException ex) {
            return new DynamicResponse(false, MessageReference.create(FlexChat.class, "hooks.towny.errors.not_in_town"));
        }
    }

}