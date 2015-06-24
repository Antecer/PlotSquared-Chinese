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
package com.intellectualcrafters.plot.database.plotme;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.TaskManager;

/**
 * Created 2014-08-17 for PlotSquared
 *
 * @author Citymonstret
 * @author Empire92
 */
public class LikePlotMeConverter {
    private String plugin;

    /**
     * Constructor
     *
     * @param plugin Plugin Used to run the converter
     */
    public LikePlotMeConverter(String plugin) {
        this.plugin = plugin;
    }
    
    private void sendMessage(final String message) {
        PlotSquared.log("&3PlotMe&8->&3PlotSquared&8: &7" + message);
    }
    
    public String getPlotMePath() {
        return new File(".").getAbsolutePath() + File.separator + "plugins" + File.separator + plugin + File.separator;
    }
    
    public String getAthionPlotsPath() {
        return new File(".").getAbsolutePath() + File.separator + "plugins" + File.separator + plugin + File.separator;
    }
    
    public FileConfiguration getPlotMeConfig(String dataFolder) {
        final File plotMeFile = new File(dataFolder + "config.yml");
        if (!plotMeFile.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(plotMeFile);
    }
    
    public Set<String> getPlotMeWorlds(FileConfiguration plotConfig) {
        return plotConfig.getConfigurationSection("worlds").getKeys(false);
    }
    
    public void updateWorldYml(String plugin, String location) {
        try {
            Path path = Paths.get(location);
            File file = new File(location);
            if (!file.exists()) {
                return;
            }
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("PlotMe-DefaultGenerator", "PlotSquared");
            content = content.replaceAll(plugin, "PlotSquared");
            Files.write(path, content.getBytes(charset));
        } catch (Exception e) {}
    }
    
    public boolean run(final APlotMeConnector connector) {
        try {
            String dataFolder = getPlotMePath();
            FileConfiguration plotConfig = getPlotMeConfig(dataFolder);
            
            if (plotConfig == null) {
                return false;
            }
            
            Connection connection = connector.getPlotMeConnection(plugin, plotConfig, dataFolder);
            
            if (connection == null) {
                sendMessage("Cannot connect to PlotMe DB. Conversion process will not continue");
                return false;
            }
            
            sendMessage(plugin + " conversion has started. To disable this, please set 'plotme-convert.enabled' in the 'settings.yml'");
            sendMessage("Connecting to " + plugin + " DB");
            
            int plotCount = 0;
            final ArrayList<Plot> createdPlots = new ArrayList<>();
            
            sendMessage("Collecting plot data");
            
            String dbPrefix = plugin.toLowerCase();
            sendMessage(" - " + dbPrefix + "Plots");
            final Set<String> worlds = getPlotMeWorlds(plotConfig);
            
            sendMessage("Updating bukkit.yml");
            updateWorldYml(plugin, "bukkit.yml");
            updateWorldYml(plugin, "plugins/Multiverse-Core/worlds.yml");
            
            for (final String world : plotConfig.getConfigurationSection("worlds").getKeys(false)) {
                sendMessage("Copying config for: " + world);
                try {
                    String actualWorldName = getWorld(world);
                    final Integer pathwidth = plotConfig.getInt("worlds." + world + ".PathWidth"); //
                    PlotSquared.config.set("worlds." + actualWorldName + ".road.width", pathwidth);
                    final Integer plotsize = plotConfig.getInt("worlds." + world + ".PlotSize"); //
                    PlotSquared.config.set("worlds." + actualWorldName + ".plot.size", plotsize);
                    final String wallblock = plotConfig.getString("worlds." + world + ".WallBlockId"); //
                    PlotSquared.config.set("worlds." + actualWorldName + ".wall.block", wallblock);
                    final String floor = plotConfig.getString("worlds." + world + ".PlotFloorBlockId"); //
                    PlotSquared.config.set("worlds." + actualWorldName + ".plot.floor", Arrays.asList(floor));
                    final String filling = plotConfig.getString("worlds." + world + ".PlotFillingBlockId"); //
                    PlotSquared.config.set("worlds." + actualWorldName + ".plot.filling", Arrays.asList(filling));
                    final String road = plotConfig.getString("worlds." + world + ".RoadMainBlockId");
                    PlotSquared.config.set("worlds." + actualWorldName + ".road.block", road);
                    Integer height = plotConfig.getInt("worlds." + world + ".RoadHeight"); //
                    if (height == null) {
                        height = 64;
                    }
                    PlotSquared.config.set("worlds." + actualWorldName + ".road.height", height);
                    PlotSquared.config.save(PlotSquared.configFile);
                } catch (final Exception e) {
                    sendMessage("&c-- &lFailed to save configuration for world '" + world + "'\nThis will need to be done using the setup command, or manually");
                }
            }
            
            HashMap<String, HashMap<PlotId, Plot>> plots = connector.getPlotMePlots(connection);
            for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
                plotCount += entry.getValue().size();
            }
            if (!Settings.CONVERT_PLOTME) {
                return false;
            }
            
            sendMessage(" - " + dbPrefix + "Allowed");
            
            sendMessage("Collected " + plotCount + " plots from PlotMe");
            final File PLOTME_DG_FILE = new File(dataFolder + File.separator + "PlotMe-DefaultGenerator" + File.separator + "config.yml");
            if (PLOTME_DG_FILE.exists()) {
                final YamlConfiguration PLOTME_DG_YML = YamlConfiguration.loadConfiguration(PLOTME_DG_FILE);
                try {
                    for (final String world : plots.keySet()) {
                        String actualWorldName = getWorld(world);
                        final String plotMeWorldName = world.toLowerCase();
                        Integer pathwidth = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".PathWidth"); //
                        if (pathwidth == null) {
                            pathwidth = 7;
                        }
                        PlotSquared.config.set("worlds." + world + ".road.width", pathwidth);
                        Integer plotsize = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".PlotSize"); //
                        if (plotsize == null) {
                            plotsize = 32;
                        }
                        PlotSquared.config.set("worlds." + world + ".plot.size", plotsize);
                        String wallblock = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".WallBlock"); //
                        if (wallblock == null) {
                            wallblock = "44";
                        }
                        PlotSquared.config.set("worlds." + world + ".wall.block", wallblock);
                        String floor = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".PlotFloorBlock"); //
                        if (floor == null) {
                            floor = "2";
                        }
                        PlotSquared.config.set("worlds." + world + ".plot.floor", Arrays.asList(floor));
                        String filling = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".FillBlock"); //
                        if (filling == null) {
                            filling = "3";
                        }
                        PlotSquared.config.set("worlds." + world + ".plot.filling", Arrays.asList(filling));
                        String road = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".RoadMainBlock");
                        if (road == null) {
                            road = "5";
                        }
                        PlotSquared.config.set("worlds." + world + ".road.block", road);
                        Integer height = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".RoadHeight"); //
                        if ((height == null) || (height == 0)) {
                            height = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".GroundHeight"); //
                            if ((height == null) || (height == 0)) {
                                height = 64;
                            }
                        }
                        PlotSquared.config.set("worlds." + actualWorldName + ".road.height", height);
                        PlotSquared.config.set("worlds." + actualWorldName + ".plot.height", height);
                        PlotSquared.config.set("worlds." + actualWorldName + ".wall.height", height);
                        PlotSquared.config.save(PlotSquared.configFile);
                    }
                } catch (final Exception e) {
                }
            }
            for (final String world : plots.keySet()) {
                int duplicate = 0;
                for (final Plot plot : plots.get(world).values()) {
                    if (!PlotSquared.getPlots(world).containsKey(plot.id)) {
                        createdPlots.add(plot);
                    } else {
                        duplicate++;
                    }
                }
                if (duplicate > 0) {
                    PlotSquared.log("&c[WARNING] Found " + duplicate + " duplicate plots already in DB for world: '" + world + "'. Have you run the converter already?");
                }
            }
            sendMessage("Creating plot DB");
            Thread.sleep(1000);
            DBFunc.createPlotsAndData(createdPlots, new Runnable() {
                @Override
                public void run() {
                    sendMessage("&aDatabase conversion is now complete!");
                    PlotSquared.log("&c - Stop the server");
                    PlotSquared.log("&c - Disable 'plotme-convert.enabled' and 'plotme-convert.cache-uuids' in the settings.yml");
                    PlotSquared.log("&c - Correct any generator settings that haven't copied to 'settings.yml' properly");
                    PlotSquared.log("&c - Start the server");
                    PlotSquared.setAllPlotsRaw(DBFunc.getPlots());
                }
            });
            sendMessage("Saving configuration...");
            try {
                PlotSquared.config.save(PlotSquared.configFile);
            } catch (final IOException e) {
                sendMessage(" - &cFailed to save configuration.");
            }
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean MV = false;
                        boolean MW = false;
                        if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                            MV = true;
                        } else if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                            MW = true;
                        }
                        for (final String worldname : worlds) {
                            final World world = Bukkit.getWorld(getWorld(worldname));
                            if (world == null) {
                                sendMessage("&cInvalid world in PlotMe configuration: " + worldname);
                            }
                            final String actualWorldName = world.getName();
                            sendMessage("Reloading generator for world: '" + actualWorldName + "'...");
                            PlotSquared.removePlotWorld(actualWorldName);
                            if (MV) {
                                // unload world with MV
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv unload " + actualWorldName);
                                try {
                                    Thread.sleep(1000);
                                } catch (final InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                                // load world with MV
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv import " + actualWorldName + " normal -g PlotSquared");
                            } else if (MW) {
                                // unload world with MW
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw unload " + actualWorldName);
                                try {
                                    Thread.sleep(1000);
                                } catch (final InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                                // load world with MW
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + actualWorldName + " plugin:PlotSquared");
                            } else {
                                // Load using Bukkit API
                                // - User must set generator manually
                                Bukkit.getServer().unloadWorld(world, true);
                                final World myworld = WorldCreator.name(actualWorldName).generator(new HybridGen(actualWorldName)).createWorld();
                                myworld.save();
                            }
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    sendMessage("&cPlease wait until database conversion is complete. You will be notified with instructions when this happens!");
                }
            });
        } catch (final Exception e) {
            PlotSquared.log("&/end/");
        }
        return true;
    }

    public static String getWorld(final String world) {
        for (final World newworld : Bukkit.getWorlds()) {
            if (newworld.getName().equalsIgnoreCase(world)) {
                return newworld.getName();
            }
        }
        return world;
    }
}
