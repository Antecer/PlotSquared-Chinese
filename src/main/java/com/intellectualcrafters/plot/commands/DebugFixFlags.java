////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;

public class DebugFixFlags extends SubCommand {
    public DebugFixFlags() {
        super(Command.DEBUGFIXFLAGS, "尝试修复世界的所有标识", "debugclear", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr != null) {
            MainUtil.sendMessage(plr, C.NOT_CONSOLE);
            return false;
        }
        if (args.length != 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot debugfixflags <世界名称>");
            return false;
        }
        final String world = args[0];
        if (!BlockManager.manager.isWorld(world) || !PlotSquared.isPlotWorld(world)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD, args[0]);
            return false;
        }
        MainUtil.sendMessage(plr, "&8--- &6开始进程 &8 ---");
        for (final Plot plot : PlotSquared.getPlots(world).values()) {
            final HashMap<String, Flag> flags = plot.settings.flags;
            Iterator<Entry<String, Flag>> i = flags.entrySet().iterator();
            boolean changed = false;
            while (i.hasNext()) {
                if (FlagManager.getFlag(i.next().getKey()) == null) {
                    changed = true;
                    i.remove();
                }
            }
            if (changed) {
                DBFunc.setFlags(plot.world, plot, plot.settings.flags.values());
            }
        }
        MainUtil.sendMessage(plr, "&a已完成!");
        return true;
    }
}
