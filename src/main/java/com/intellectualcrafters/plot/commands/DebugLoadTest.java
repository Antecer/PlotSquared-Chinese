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

import java.lang.reflect.Field;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * @author Citymonstret
 */
public class DebugLoadTest extends SubCommand {
    public DebugLoadTest() {
        super(Command.DEBUGLOADTEST, "这个调试指令将强制重读所有数据库中的地皮", "debugloadtest", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr == null) {
            try {
                final Field fPlots = PlotSquared.class.getDeclaredField("plots");
                fPlots.setAccessible(true);
                fPlots.set(null, DBFunc.getPlots());
            } catch (final Exception e) {
                PlotSquared.log("&3==== 请无视下方报错代码 &3====");
                e.printStackTrace();
                PlotSquared.log("&3==== 请无视上方报错代码 &3====");
            }
        } else {
            MainUtil.sendMessage(plr, "&6这个指令只能通过控制台使用.");
        }
        return true;
    }
}
