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
package com.intellectualcrafters.plot.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;

/**
 * @author Citymonstret
 * @author Empire92
 */
public interface AbstractDB {
    // TODO MongoDB @Brandon
    /**
     * The UUID that will count as everyone
     */
    UUID everyone = UUID.fromString("1-1-3-3-7");

    /**
     * Set Plot owner
     *
     * @param plot Plot in which the owner should be set
     * @param uuid The uuid of the new owner
     */
    void setOwner(final Plot plot, final UUID uuid);

    /**
     * Create all settings, and create default helpers, trusted + denied lists
     *
     * @param plots Plots for which the default table entries should be created
     */
    void createPlotsAndData(final ArrayList<Plot> plots, Runnable whenDone);

    /**
     * Create a plot
     *
     * @param plot That should be created
     */
    void createPlot(final Plot plot);

    /**
     * Create tables
     *
     * @param database Database in which the tables will be created
     *
     * @throws SQLException If the database manager is unable to create the tables
     */
    void createTables(final String database) throws Exception;

    /**
     * Delete a plot
     *
     * @param plot Plot that should be deleted
     */
    void delete(final String world, final Plot plot);

    void delete(final PlotCluster cluster);

    /**
     * Create plot settings
     *
     * @param id   Plot Entry ID
     * @param plot Plot Object
     */
    void createPlotSettings(final int id, final Plot plot);

    /**
     * Get the table entry ID
     *
     * @param world Which the plot is located in
     * @param id2   Plot ID
     *
     * @return Integer = Plot Entry Id
     */
    int getId(final String world, final PlotId id2);

    /**
     * Get the id of a given plot cluster
     *
     * @param world Which the plot is located in
     * @param id cluster id
     *
     * @return Integer = Cluster Entry Id
     */
    int getClusterId(final String world, final PlotClusterId id);

    /**
     * @return A linked hashmap containing all plots
     */
    LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots();

    /**
     * @return A hashmap containing all plot clusters
     */
    HashMap<String, HashSet<PlotCluster>> getClusters();

    /**
     * Set the merged status for a plot
     *
     * @param world  World in which the plot is located
     * @param plot   Plot Object
     * @param merged boolean[]
     */
    void setMerged(final String world, final Plot plot, final boolean[] merged);

    /**
     * Swap the settings, helpers etc. of two plots
     * @param p1 Plot1
     * @param p2 Plot2
     */
    void swapPlots(final Plot p1, final Plot p2);

    /**
     * Set plot flags
     *
     * @param world World in which the plot is located
     * @param plot  Plot Object
     * @param flags flags to set (flag[])
     */
    void setFlags(final String world, final Plot plot, final Set<Flag> flags);

    /**
     * Set cluster flags
     *
     * @param cluster PlotCluster Object
     * @param flags flags to set (flag[])
     */
    void setFlags(final PlotCluster cluster, final Set<Flag> flags);

    /**
     * Rename a cluster
     */
    void setClusterName(final PlotCluster cluster, final String name);

    /**
     * Set the plot alias
     *
     * @param plot  Plot for which the alias should be set
     * @param alias Plot Alias
     */
    void setAlias(final String world, final Plot plot, final String alias);

    /**
     * Purgle a plot
     *
     * @param world World in which the plot is located
     * @param uniqueIds list of plot id (db) to be purged
     */
    void purgeIds(final String world, final Set<Integer> uniqueIds);

    /**
     * Purge a whole world
     *
     * @param world World in which the plots should be purged
     */
    void purge(final String world, final Set<PlotId> plotIds);

    /**
     * Set Plot Home Position
     *
     * @param plot     Plot Object
     * @param position Plot Home Position
     */
    void setPosition(final String world, final Plot plot, final String position);

    /**
     *
     * @param cluster
     * @param position
     */
    void setPosition(final PlotCluster cluster, final String position);

    /**
     * @param id Plot Entry ID
     *
     * @return Plot Settings
     */
    HashMap<String, Object> getSettings(final int id);

    /**
     *
     * @param id
     * @return HashMap<String, Object>
     */
    HashMap<String, Object> getClusterSettings(final int id);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeTrusted(final String world, final Plot plot, final UUID uuid);

    /**
     * @param cluster   PlotCluster Object
     * @param uuid Player that should be removed
     */
    void removeHelper(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeMember(final String world, final Plot plot, final UUID uuid);

    /**
     *
     * @param cluster
     * @param uuid
     */
    void removeInvited(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void setTrusted(final String world, final Plot plot, final UUID uuid);

    /**
     * @param cluster PlotCluster Object
     * @param uuid Player that should be removed
     */
    void setHelper(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be added
     */
    void setMember(final String world, final Plot plot, final UUID uuid);

    /**
     *
     * @param world
     * @param cluster
     * @param uuid
     */
    void setInvited(final String world, final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid   Player uuid
     */
    void removeDenied(final String world, final Plot plot, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player uuid that should be added
     */
    void setDenied(final String world, final Plot plot, final UUID uuid);

    /**
     * Get Plots ratings
     *
     * @param plot Plot Object
     *
     * @return Plot Ratings (pre-calculated)
     */
    double getRatings(final Plot plot);
    
    /**
     * if uuid has rated
     * @param uuid
     * @return
     */
    boolean hasRated(String world, PlotId id, UUID uuid);
    
    /**
     * Set a rating for a plot
     * @param plot
     * @param rater
     * @param value
     */
    void setRating(final Plot plot, UUID rater, int value);

    /**
     * Remove a plot comment
     *
     * @param world   World in which the plot is located
     * @param plot    Plot Object
     * @param comment Comment to remove
     */
    void removeComment(final String world, final Plot plot, final PlotComment comment);
    
    /**
     * Clear an inbox
     * @param plot
     * @param inbox
     */
    void clearInbox(Plot plot, String inbox);

    /**
     * Set a plot comment
     * 
     * @param world   World in which the plot is located
     * @param plot    Plot Object
     * @param comment Comment to add
     */
    void setComment(final String world, final Plot plot, final PlotComment comment);

    /**
     * Get Plot Comments
     *
     * @param world World in which the plot is located
     * @param plot  Plot Object
     * @param tier  Comment Tier
     *
     * @return Plot Comments within the specified tier
     */
    void getComments(final String world, final Plot plot, final String inbox, RunnableVal whenDone);

    void createPlotAndSettings(Plot plot);

    void createCluster(PlotCluster cluster);

    void resizeCluster(PlotCluster current, PlotClusterId resize);

    void movePlot(Plot originalPlot, Plot newPlot);
    
    /**
     * Don't fuck with this one, unless you enjoy it rough
     */
    boolean deleteTables();
}
