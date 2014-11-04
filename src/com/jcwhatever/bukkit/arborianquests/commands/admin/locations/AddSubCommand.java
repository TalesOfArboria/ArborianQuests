/* This file is part of ArborianQuests for Bukkit, licensed under the MIT License (MIT).
 * 
 * Copyright (c) JCThePants (www.jcwhatever.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.arborianquests.commands.admin.locations;

import com.jcwhatever.bukkit.arborianquests.ArborianQuests;
import com.jcwhatever.bukkit.arborianquests.Lang;
import com.jcwhatever.bukkit.arborianquests.locations.ScriptLocation;
import com.jcwhatever.bukkit.arborianquests.locations.ScriptLocationManager;
import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.arguments.LocationResponse;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent="locations",
        command = "add",
        staticParams = { "locationName", "location" },
        usage = "/{plugin-command} {command} add <locationName> <location>",
        description = "Add a new quest location.")

public class AddSubCommand extends AbstractCommand {

    @Localizable static final String _NOT_CONSOLE = "Console can't select a location.";
    @Localizable static final String _LOCATION_ALREADY_EXISTS = "There is already a location with the name '{0}'.";
    @Localizable static final String _FAILED = "Failed to add location.";
    @Localizable static final String _SUCCESS = "Quest location '{0}' created.";

    @Override
    public void execute (CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, Lang.get(_NOT_CONSOLE));

        final String locationName = args.getName("locationName", 32);

        final ScriptLocationManager manager = ArborianQuests.getPlugin().getScriptLocationManager();

        ScriptLocation scriptLocation = manager.getLocation(locationName);
        if (scriptLocation != null) {
            tellError(sender, Lang.get(_LOCATION_ALREADY_EXISTS), locationName);
            return; // finished
        }

        args.getLocation(sender, "location", new LocationResponse() {

            @Override
            public void onLocationRetrieved(Player p, Location result) {

                ScriptLocation scriptLocation = manager.addLocation(locationName, result);
                if (scriptLocation == null) {
                    tellError(p, Lang.get(_FAILED));
                }
                else {
                    tellSuccess(p, Lang.get(_SUCCESS), locationName);
                }

            }
        });

    }
}
