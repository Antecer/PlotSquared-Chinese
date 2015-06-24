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
package com.intellectualcrafters.plot.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;

/**
 * The plot class
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("javadoc")
public class Plot implements Cloneable {
    /**
     * plot ID
     */
    public final PlotId id;
    /**
     * plot world
     */
    public final String world;
    /**
     * plot owner
     */
    public UUID owner;
    /**
     * Deny Entry
     */
    public boolean deny_entry;
    /**
     * List of trusted (with plot permissions)
     */
    public ArrayList<UUID> trusted;
    /**
     * List of members users (with plot permissions)
     */
    public ArrayList<UUID> members;
    /**
     * List of denied players
     */
    public ArrayList<UUID> denied;
    /**
     * External settings class
     */
    public PlotSettings settings;
    /**
     * Delete on next save cycle?
     */
    public boolean delete;
    /**
     * Has the plot changed since the last save cycle?
     */
    public boolean hasChanged = false;
    public boolean countsTowardsMax = true;

    /**
     * Primary constructor
     *
     * @param id
     * @param owner
     * @param trusted
     * @param denied
     */
    public Plot(final PlotId id, final UUID owner, final ArrayList<UUID> trusted, final ArrayList<UUID> denied, final String world) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner == null;
        this.trusted = trusted;
        this.denied = denied;
        this.members = new ArrayList<>();
        this.settings.setAlias("");
        this.delete = false;
        this.settings.flags = new HashMap<>();
        this.world = world;
    }

    /**
     * Constructor for saved plots
     *
     * @param id
     * @param owner
     * @param trusted
     * @param denied
     * @param merged
     */
    public Plot(final PlotId id, final UUID owner, final ArrayList<UUID> trusted, final ArrayList<UUID> members, final ArrayList<UUID> denied, final String alias, final BlockLoc position, final Collection<Flag> flags, final String world, final boolean[] merged) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner != null;
        this.members = members;
        this.trusted = trusted;
        this.denied = denied;
        this.settings.setAlias(alias);
        this.settings.setPosition(position);
        this.settings.setMerged(merged);
        this.delete = false;
        this.settings.flags = new HashMap<>();
        if (flags != null) {
            for (Flag flag : flags) {
                this.settings.flags.put(flag.getKey(), flag);
            }
        }
        this.world = world;
    }

    /**
     * Check if the plot has a set owner
     *
     * @return false if there is no owner
     */
    public boolean hasOwner() {
        return this.owner != null;
    }
    
    public boolean isOwner(UUID uuid) {
        return PlotHandler.isOwner(this, uuid);
    }
    
    /**
     * Get a list of owner UUIDs for a plot (supports multi-owner mega-plots)
     * @return
     */
    public HashSet<UUID> getOwners() {
        return PlotHandler.getOwners(this);
    }

    /**
     * Check if the player is either the owner or on the trusted list
     *
     * @param uuid
     *
     * @return true if the player is added as a helper or is the owner
     */
    public boolean isAdded(final UUID uuid) {
        return PlotHandler.isAdded(this, uuid);
    }

    /**
     * Should the player be allowed to enter?
     *
     * @param uuid
     *
     * @return boolean false if the player is allowed to enter
     */
    public boolean isDenied(final UUID uuid) {
        return (this.denied != null) && ((this.denied.contains(DBFunc.everyone) && !this.isAdded(uuid)) || (!this.isAdded(uuid) && this.denied.contains(uuid)));
    }

    /**
     * Get the plot ID
     */
    public PlotId getId() {
        return this.id;
    }

    /**
     * Get a clone of the plot
     *
     * @return Plot
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final Plot p = (Plot) super.clone();
        if (!p.equals(this) || (p != this)) {
            return new Plot(this.id, this.owner, this.trusted, this.members, this.denied, this.settings.getAlias(), this.settings.getPosition(), this.settings.flags.values(), this.world, this.settings.getMerged());
        }
        return p;
    }

    /**
     * Deny someone (use DBFunc.addDenied() as well)
     *
     * @param uuid
     */
    public void addDenied(final UUID uuid) {
        this.denied.add(uuid);
    }

    /**
     * Add someone as a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public void addTrusted(final UUID uuid) {
        this.trusted.add(uuid);
    }

    /**
     * Add someone as a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public void addMember(final UUID uuid) {
        this.members.add(uuid);
    }

    /**
     * Get plot display name
     *
     * @return alias if set, else id
     */
    @Override
    public String toString() {
        if (this.settings.getAlias().length() > 1) {
            return this.settings.getAlias();
        }
        return this.world + ";" + this.getId().x + ";" + this.getId().y;
    }

    /**
     * Remove a denied player (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeDenied(final UUID uuid) {
        this.denied.remove(uuid);
    }

    /**
     * Remove a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeHelper(final UUID uuid) {
        this.trusted.remove(uuid);
    }

    /**
     * Remove a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeTrusted(final UUID uuid) {
        this.members.remove(uuid);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Plot other = (Plot) obj;
        return ((this.id.x.equals(other.id.x)) && (this.id.y.equals(other.id.y)) && (this.world.equals(other.world)));
    }

    
    /**
     * Get the plot hashcode
     *
     * @return integer. You can easily make this a character array <br> xI = c[0] x = c[1 -&gt; xI...] yI = c[xI ... + 1] y
     * = c[xI ... + 2 -&gt; yI ...]
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
