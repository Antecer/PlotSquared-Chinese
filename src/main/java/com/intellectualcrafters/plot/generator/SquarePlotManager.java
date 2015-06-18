package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * A plot manager with a square grid layout, with square shaped plots
 */
public abstract class SquarePlotManager extends GridPlotManager {
    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
        ChunkManager.manager.regenerateRegion(pos1, pos2, whenDone);
        return true;
    }

    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(plotworld.worldname, x, 256, z);
    }

    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        int pathWidthLower;
        int end;
        if (dpw.ROAD_WIDTH == 0) {
            pathWidthLower = -1;
            end = dpw.PLOT_WIDTH;
        }
        else {
            if ((dpw.ROAD_WIDTH % 2) == 0) {
                pathWidthLower = (dpw.ROAD_WIDTH / 2) - 1;
            } else {
                pathWidthLower = dpw.ROAD_WIDTH / 2;
            }
            end = pathWidthLower + dpw.PLOT_WIDTH;
        }
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int idx;
        int idz;
        if (x < 0) {
            idx = (x/size);
            x = size + (x % size);
        }
        else {
            idx = (x/size) + 1;
            x = (x % size);
        }
        if (z < 0) {
            idz = (z/size);
            z = size + (z % size);
        }
        else {
            idz = (z/size) + 1;
            z = (z % size);
        }
        final boolean northSouth = (z <= pathWidthLower) || (z > end);
        final boolean eastWest = (x <= pathWidthLower) || (x > end);
        if (northSouth || eastWest) {
            return null;
        }
        return new PlotId(idx, idz);
    }

    @Override
    public PlotId getPlotId(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        if (plotworld == null) {
            return null;
        }
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int pathWidthLower;
        final int end;
        if (dpw.ROAD_WIDTH == 0) {
            pathWidthLower = -1;
            end = dpw.PLOT_WIDTH;
        }
        else {
            if ((dpw.ROAD_WIDTH % 2) == 0) {
                pathWidthLower = (dpw.ROAD_WIDTH / 2) - 1;
            } else {
                pathWidthLower = dpw.ROAD_WIDTH / 2;
            }
            end = pathWidthLower + dpw.PLOT_WIDTH;
        }
        int dx;
        int dz;
        int rx;
        int rz;
        if (x < 0) {
            dx = (x/size);
            rx = size + (x % size);
        }
        else {
            dx = (x/size) + 1;
            rx = (x % size);
        }
        if (z < 0) {
            dz = (z/size);
            rz = size + (z % size);
        }
        else {
            dz = (z/size) + 1;
            rz = (z % size);
        }
        final boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        final boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth && eastWest) {
            // This means you are in the intersection
            final Location loc = new Location(plotworld.worldname, x + dpw.ROAD_WIDTH, 0, z + dpw.ROAD_WIDTH);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var poorly)
            final Location loc = new Location(plotworld.worldname, x, 0, z + dpw.ROAD_WIDTH);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final Location loc = new Location(plotworld.worldname, x + dpw.ROAD_WIDTH, 0, z);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(dx, dz);
        final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
        if (plot == null) {
            return id;
        }
        return MainUtil.getBottomPlot(plot).id;
    }

    /**
     * Get the bottom plot loc (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(plotworld.worldname, x, 1, z);
    }

    /**
     * Set a plot biome
     */
    @Override
    public boolean setBiome(final Plot plot, final int biome) {
        final int bottomX = MainUtil.getPlotBottomLoc(plot.world, plot.id).getX() - 1;
        final int topX = MainUtil.getPlotTopLoc(plot.world, plot.id).getX() + 1;
        final int bottomZ = MainUtil.getPlotBottomLoc(plot.world, plot.id).getZ() - 1;
        final int topZ = MainUtil.getPlotTopLoc(plot.world, plot.id).getZ() + 1;
        final int size = ((topX - bottomX) + 1) * ((topZ - bottomZ) + 1);
        final int[] xb = new int[size];
        final int[] zb = new int[size];
        final int[] biomes = new int[size];
        int index = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                xb[index] = x;
                zb[index] = z;
                biomes[index] = biome;
                index++;
            }
        }
        BlockManager.setBiomes(plot.world, xb, zb, biomes);
        return true;
    }
}
