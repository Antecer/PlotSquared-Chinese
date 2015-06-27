package com.intellectualcrafters.plot.generator;

import java.io.File;
import java.util.HashMap;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;

public abstract class HybridUtils {

    public static HybridUtils manager;
    
    public abstract void checkModified(final Plot plot, final RunnableVal<Integer> whenDone);
    
    public abstract void analyzePlot(final Plot plot, final RunnableVal<PlotAnalysis> whenDone);

    public abstract int checkModified(final String world, final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks);

    public boolean setupRoadSchematic(final Plot plot) {
        final String world = plot.world;
        final Location bot = MainUtil.getPlotBottomLoc(world, plot.id);
        final Location top = MainUtil.getPlotTopLoc(world, plot.id);
        final HybridPlotWorld plotworld = (HybridPlotWorld) PlotSquared.getPlotWorld(world);
        final int sx = (bot.getX() - plotworld.ROAD_WIDTH) + 1;
        final int sz = bot.getZ() + 1;
        final int sy = plotworld.ROAD_HEIGHT;
        final int ex = bot.getX();
        final int ez = top.getZ();
        final int ey = get_ey(world, sx, ex, sz, ez, sy);
        final Location pos1 = new Location(world, sx, sy, sz);
        final Location pos2 = new Location(world, ex, ey, ez);
        final int bx = sx;
        final int bz = sz - plotworld.ROAD_WIDTH;
        final int by = sy;
        final int tx = ex;
        final int tz = sz - 1;
        final int ty = get_ey(world, bx, tx, bz, tz, by);
        final Location pos3 = new Location(world, bx, by, bz);
        final Location pos4 = new Location(world, tx, ty, tz);
        final CompoundTag sideroad = SchematicHandler.manager.getCompoundTag(world, pos1, pos2);
        final CompoundTag intersection = SchematicHandler.manager.getCompoundTag(world, pos3, pos4);
        final String dir = PlotSquared.IMP.getDirectory() + File.separator + "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.world + File.separator;
        SchematicHandler.manager.save(sideroad, dir + "sideroad.schematic");
        SchematicHandler.manager.save(intersection, dir + "intersection.schematic");
        plotworld.ROAD_SCHEMATIC_ENABLED = true;
        plotworld.setupSchematics();
        return true;
    }

    public abstract int get_ey(final String world, final int sx, final int ex, final int sz, final int ez, final int sy);

    public abstract boolean scheduleRoadUpdate(final String world, int extend);

    public boolean regenerateRoad(final String world, final ChunkLoc chunk, int extend) {
        final int x = chunk.x << 4;
        final int z = chunk.z << 4;
        final int ex = x + 15;
        final int ez = z + 15;
        final HybridPlotWorld plotworld = (HybridPlotWorld) PlotSquared.getPlotWorld(world);
        extend = Math.min(extend, 255 - plotworld.ROAD_HEIGHT - plotworld.SCHEMATIC_HEIGHT);
        if (!plotworld.ROAD_SCHEMATIC_ENABLED) {
            return false;
        }
        boolean toCheck = false;
        if (plotworld.TYPE == 2) {
            boolean c1 = MainUtil.isPlotArea(new Location(plotworld.worldname, x, 1, z));
            boolean c2 = MainUtil.isPlotArea(new Location(plotworld.worldname, ex, 1, ez));
            if (!c1 && !c2) {
                return false;
            }
            else {
                toCheck = c1 ^ c2;
            }
        }
        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotId id1 = manager.getPlotId(plotworld, x, 0, z);
        final PlotId id2 = manager.getPlotId(plotworld, ex, 0, ez);
        if ((id1 == null) || (id2 == null) || (id1 != id2)) {
            final boolean result = ChunkManager.manager.loadChunk(world, chunk);
            if (result) {
                if (id1 != null) {
                    final Plot p1 = MainUtil.getPlot(world, id1);
                    if ((p1 != null) && p1.hasOwner() && p1.settings.isMerged()) {
                        toCheck = true;
                    }
                }
                if ((id2 != null) && !toCheck) {
                    final Plot p2 = MainUtil.getPlot(world, id2);
                    if ((p2 != null) && p2.hasOwner() && p2.settings.isMerged()) {
                        toCheck = true;
                    }
                }
                final int size = plotworld.SIZE;
                for (int X = 0; X < 16; X++) {
                    for (int Z = 0; Z < 16; Z++) {
                        short absX = (short) ((x + X) % size);
                        short absZ = (short) ((z + Z) % size);
                        if (absX < 0) {
                            absX += size;
                        }
                        if (absZ < 0) {
                            absZ += size;
                        }
                        boolean condition;
                        if (toCheck) {
                            condition = manager.getPlotId(plotworld, x + X, 1, z + Z) == null;
//                            condition = MainUtil.isPlotRoad(new Location(plotworld.worldname, x + X, 1, z + Z));
                        } else {
                            final boolean gx = absX > plotworld.PATH_WIDTH_LOWER;
                            final boolean gz = absZ > plotworld.PATH_WIDTH_LOWER;
                            final boolean lx = absX < plotworld.PATH_WIDTH_UPPER;
                            final boolean lz = absZ < plotworld.PATH_WIDTH_UPPER;
                            condition = (!gx || !gz || !lx || !lz);
                        }
                        if (condition) {
                            final int sy = plotworld.ROAD_HEIGHT;
                            final PlotLoc loc = new PlotLoc(absX, absZ);
                            final HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                            for (short y = (short) (plotworld.ROAD_HEIGHT); y <= (plotworld.ROAD_HEIGHT + plotworld.SCHEMATIC_HEIGHT + extend); y++) {
                                BlockManager.manager.functionSetBlock(world, x + X, y, z + Z, 0, (byte) 0);
                            }
                            if (blocks != null) {
                                final HashMap<Short, Byte> datas = plotworld.G_SCH_DATA.get(loc);
                                if (datas == null) {
                                    for (final Short y : blocks.keySet()) {
                                        BlockManager.manager.functionSetBlock(world, x + X, sy + y, z + Z, blocks.get(y), (byte) 0);
                                    }
                                } else {
                                    for (final Short y : blocks.keySet()) {
                                        Byte data = datas.get(y);
                                        if (data == null) {
                                            data = 0;
                                        }
                                        BlockManager.manager.functionSetBlock(world, x + X, sy + y, z + Z, blocks.get(y), data);
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
