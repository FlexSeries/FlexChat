/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexchat.commands.ignore;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SCmdIgnoreList extends Subcommand<FlexChat> {

    public SCmdIgnoreList(FlexCommand<FlexChat> parent) {
        super(parent, new CommandDescriptor("list").playerOnly(true).description("List ignored players"));
    }

    @Override
    public void handleExecute(CommandContext context) {
        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);

        List<String> ignored = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(((Player) context.getSender()).getUniqueId()).getCustomData("ignored", List.class);
        if (ignored == null) {
            ignored = new ArrayList<>();
        }

        List<String> names = new ArrayList<>();
        for (String identifier : ignored) {
            UUID uuid;
            try {
                uuid = UUID.fromString(identifier);
            } catch (Exception ex) {
                names.add(identifier);
                continue;
            }

            names.add(uuidTracker.getLatestName(uuid));
        }

        Collections.sort(names);

        String namesFormatted = StringUtils.collectionToString(names, ChatColor.GRAY + ", ");

        ListBuilder builder = new ListBuilder("title", "Ignored", null, context.getLabel());
        if (!names.isEmpty()) {
            builder.addMessage(namesFormatted);
        }
        builder.sendTo(context.getSender(), 1);
    }

}