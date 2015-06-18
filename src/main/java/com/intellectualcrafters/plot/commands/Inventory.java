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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class Inventory extends SubCommand {
    public Inventory() {
        super("inventory", "plots.inventory", "打开指令菜单", "inventory", "inv", CommandCategory.INFO, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        final ArrayList<SubCommand> cmds = new ArrayList<>();
        for (final SubCommand cmd : MainCommand.subCommands) {
            if (cmd.permission.hasPermission(plr)) {
                cmds.add(cmd);
            }
        }
        final int size = 9 * (int) Math.ceil(cmds.size() / 9.0);
        final org.bukkit.inventory.Inventory inventory = Bukkit.createInventory(null, size, "PlotSquared 指令菜单");
        for (final SubCommand cmd : cmds) {
            inventory.addItem(getItem(cmd));
        }
        ((BukkitPlayer) plr).player.openInventory(inventory);
        return true;
    }

    private ItemStack getItem(final SubCommand cmd) {
        final ItemStack stack = new ItemStack(Material.COMMAND);
        final ItemMeta meta = stack.getItemMeta();
        {
            meta.setDisplayName(ChatColor.GREEN + cmd.cmd + ChatColor.DARK_GRAY + " [" + ChatColor.GREEN + cmd.alias + ChatColor.DARK_GRAY + "]");
            meta.setLore(new ArrayList<String>() {
                {
                    add(C.INVENTORY_CATEGORY.s().replace("{category}", cmd.category.toString()));
                    add(C.INVENTORY_DESC.s().replace("{desc}", cmd.description));
                    add(C.INVENTORY_USAGE.s().replace("{usage}", "/plot " + cmd.usage));
                }
            });
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
