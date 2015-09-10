/**
 * FlexChat - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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