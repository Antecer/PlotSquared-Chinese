package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;

public abstract class ChunkManager {
    
    public static ChunkManager manager = null;
    public static RegionWrapper CURRENT_PLOT_CLEAR = null;
    public static boolean FORCE_PASTE = false;
    
    public static HashMap<PlotLoc, HashMap<Short, Short>> GENERATE_BLOCKS = new HashMap<>();
    public static HashMap<PlotLoc, HashMap<Short, Byte>> GENERATE_DATA = new HashMap<>();

    public static ChunkLoc getChunkChunk(final Location loc) {
        final int x = loc.getX() >> 9;
        final int z = loc.getZ() >> 9;
        return new ChunkLoc(x, z);
    }
    
    public abstract void setChunk(ChunkWrapper loc, PlotBlock[][] result);
    
    public abstract int[] countEntities(Plot plot);

    public abstract boolean loadChunk(String world, ChunkLoc loc);
    
    public abstract boolean unloadChunk(String world, ChunkLoc loc);

    public abstract List<ChunkLoc> getChunkChunks(String world);

    public abstract void regenerateChunk(String world, ChunkLoc loc);

    public abstract void deleteRegionFile(final String world, final ChunkLoc loc);
    
    public abstract void deleteRegionFiles(final String world, final List<ChunkLoc> chunks);

    public abstract Plot hasPlot(String world, ChunkLoc chunk);

    public abstract boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone);

    public abstract boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone);

    public abstract void clearAllEntities(final Plot plot);
    
    public abstract void swap(String world, PlotId id, PlotId plotid);

    public abstract void swap(String worldname, Location bot1, Location top1, Location bot2, Location top2);
}
