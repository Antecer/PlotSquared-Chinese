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
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.InfoInventory;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
@SuppressWarnings({ "javadoc" })
public class Info extends SubCommand {
    public Info() {
        super(Command.INFO, "显示地皮信息", "info", CommandCategory.INFO, false);
    }

    @Override
    public boolean execute(final PlotPlayer player, String... args) {
        Plot plot;
        String world;
        if (player != null) {
            final Location loc = player.getLocation();
            world = loc.getWorld();
            if (!PlotSquared.isPlotWorld(world)) {
                MainUtil.sendMessage(player, C.NOT_IN_PLOT_WORLD);
                return false;
            }
            plot = MainUtil.getPlot(loc);
            if (plot == null) {
                return !sendMessage(player, C.NOT_IN_PLOT);
            }
        } else {
            if (args.length < 2) {
                MainUtil.sendMessage(null, C.INFO_SYNTAX_CONSOLE);
                return false;
            }
            final PlotWorld plotworld = PlotSquared.getPlotWorld(args[0]);
            if (plotworld == null) {
                MainUtil.sendMessage(player, C.NOT_VALID_WORLD);
                return false;
            }
            try {
                final String[] split = args[1].split(";");
                final PlotId id = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                plot = MainUtil.getPlot(plotworld.worldname, id);
                if (plot == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                world = args[0];
                if (args.length == 3) {
                    args = new String[] { args[2] };
                } else {
                    args = new String[0];
                }
            } catch (final Exception e) {
                MainUtil.sendMessage(player, C.INFO_SYNTAX_CONSOLE);
                return false;
            }
        }
        if ((args.length == 1) && args[0].equalsIgnoreCase("inv")) {
            new InfoInventory(plot, player).build().display(); 
            return true;
        }
        final boolean hasOwner = plot.hasOwner();
        boolean containsEveryone;
        boolean trustedEveryone;
        // Wildcard player {added}
        {
            containsEveryone = (plot.trusted != null) && plot.trusted.contains(DBFunc.everyone);
            trustedEveryone = (plot.members != null) && plot.members.contains(DBFunc.everyone);
        }
        // Unclaimed?
        if (!hasOwner && !containsEveryone && !trustedEveryone) {
            MainUtil.sendMessage(player, C.PLOT_INFO_UNCLAIMED, (plot.id.x + ";" + plot.id.y));
            return true;
        }
        String owner = "none";
        if (plot.owner == null) {
            owner = "unowned";
        }
        else {
            owner = getPlayerList(plot.getOwners());
        }
        String info = C.PLOT_INFO.s();
        if (args.length == 1) {
            info = getCaption(args[0].toLowerCase());
            if (info == null) {
                MainUtil.sendMessage(player, "&6分类s&7: &amembers&7, &aalias&7, &abiome&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, &aowner&7, &arating");
                return false;
            }
        }
        formatAndSend(info, world, plot, player);
        return true;
    }

    private String getCaption(final String string) {
        switch (string) {
            case "trusted":
                return C.PLOT_INFO_TRUSTED.s();
            case "alias":
                return C.PLOT_INFO_ALIAS.s();
            case "biome":
                return C.PLOT_INFO_BIOME.s();
            case "denied":
                return C.PLOT_INFO_DENIED.s();
            case "flags":
                return C.PLOT_INFO_FLAGS.s();
            case "id":
                return C.PLOT_INFO_ID.s();
            case "size":
                return C.PLOT_INFO_SIZE.s();
            case "members":
                return C.PLOT_INFO_MEMBERS.s();
            case "owner":
                return C.PLOT_INFO_OWNER.s();
            case "rating":
                return C.PLOT_INFO_RATING.s();
            default:
                return null;
        }
    }

    private void formatAndSend(String info, final String world, final Plot plot, final PlotPlayer player) {
        final PlotId id = plot.id;
        final PlotId id2 = MainUtil.getTopPlot(plot).id;
        final int num = MainUtil.getPlotSelectionIds(id, id2).size();
        final String alias = plot.settings.getAlias().length() > 0 ? plot.settings.getAlias() : "none";
        Location top = MainUtil.getPlotTopLoc(world, plot.id);
        Location bot = MainUtil.getPlotBottomLoc(world, plot.id).add(1,0,1);
        final String biome = BlockManager.manager.getBiome(bot.add((top.getX() - bot.getX()) / 2, 0, (top.getX() - bot.getX()) / 2));
        final String trusted = getPlayerList(plot.trusted);
        final String members = getPlayerList(plot.members);
        final String denied = getPlayerList(plot.denied);
        
        final String flags = "&6" + (StringUtils.join(FlagManager.getPlotFlags(plot), "").length() > 0 ? StringUtils.join(FlagManager.getPlotFlags(plot), "&7, &6") : "none");
        final boolean build = (player == null) || plot.isAdded(player.getUUID());
        String owner = "none";
        if (plot.owner == null) {
            owner = "unowned";
        }
        else {
            owner = getPlayerList(plot.getOwners());
        }
        info = info.replaceAll("%alias%", alias);
        info = info.replaceAll("%id%", id.toString());
        info = info.replaceAll("%id2%", id2.toString());
        info = info.replaceAll("%num%", num + "");
        info = info.replaceAll("%biome%", biome);
        info = info.replaceAll("%owner%", owner);
        info = info.replaceAll("%members%", members);
        info = info.replaceAll("%trusted%", trusted);
        info = info.replaceAll("%helpers%", members);
        info = info.replaceAll("%denied%", denied);
        info = info.replaceAll("%flags%", Matcher.quoteReplacement(flags));
        info = info.replaceAll("%build%", build + "");
        info = info.replaceAll("%desc%", "No description set.");

        if (info.contains("%rating%")) {
            final String newInfo = info;
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    String info = newInfo.replaceAll("%rating%", String.format("%.1f", DBFunc.getRatings(plot)));
                    MainUtil.sendMessage(player, C.PLOT_INFO_HEADER);
                    MainUtil.sendMessage(player, info, false);
                }
            });
            return;
        }
        MainUtil.sendMessage(player, C.PLOT_INFO_HEADER);
        MainUtil.sendMessage(player, info, false);
    }

    private String getPlayerList(final Collection<UUID> uuids) {
        ArrayList<UUID> l = new ArrayList<>(uuids);
        if ((l == null) || (l.size() < 1)) {
            return " none";
        }
        final String c = C.PLOT_USER_LIST.s();
        final StringBuilder list = new StringBuilder();
        for (int x = 0; x < l.size(); x++) {
            if ((x + 1) == l.size()) {
                list.append(c.replace("%user%", getPlayerName(l.get(x))).replace(",", ""));
            } else {
                list.append(c.replace("%user%", getPlayerName(l.get(x))));
            }
        }
        return list.toString();
    }

    private String getPlayerName(final UUID uuid) {
        if (uuid == null) {
            return "unknown";
        }
        if (uuid.equals(DBFunc.everyone) || uuid.toString().equalsIgnoreCase(DBFunc.everyone.toString())) {
            return "everyone";
        }
        final String name = UUIDHandler.getName(uuid);
        if (name == null) {
            return "unknown";
        }
        return name;
    }
}
