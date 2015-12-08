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
package me.st28.flexseries.flexchat.commands.arguments;

import me.st28.flexseries.flexchat.api.chatter.Chatter;
import me.st28.flexseries.flexchat.backend.chatter.ChatterManagerImpl;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.argument.PlayerArgument;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.Bukkit;

public class ChatterArgument extends PlayerArgument {

    public ChatterArgument(String name, boolean isRequired) {
        super(name, isRequired);

        onlineOnly(true);
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        ChatterManagerImpl chatterManager = FlexPlugin.getGlobalModule(ChatterManagerImpl.class);

        Chatter targetChatter;

        if (input.equalsIgnoreCase("console")) {
            targetChatter = chatterManager.getChatter(Bukkit.getConsoleSender());
        } else {
            PlayerReference ref = (PlayerReference) super.parseInput(context, input);

            targetChatter = chatterManager.getChatter(ref.getPlayer());
        }

        return targetChatter;
    }

}