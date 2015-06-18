package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.mutable.MutableInt;

import com.intellectualcrafters.plot.PlotSquared;

public abstract class TaskManager {
    public static HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    
    public static MutableInt index = new MutableInt(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();

    public abstract int taskRepeat(final Runnable r, int interval);

    public abstract void taskAsync(final Runnable r);

    public abstract void task(final Runnable r);

    public abstract void taskLater(final Runnable r, int delay);

    public abstract void taskLaterAsync(final Runnable r, int delay);

    public abstract void cancelTask(int task);

    public static int runTaskRepeat(final Runnable r, final int interval) {
        if (r != null) {
            return PlotSquared.TASK.taskRepeat(r, interval);
        }
        return -1;
    }

    public static void runTaskAsync(final Runnable r) {
        if (r != null) {
            PlotSquared.TASK.taskAsync(r);
        }
    }

    public static void runTask(final Runnable r) {
        if (r != null) {
            PlotSquared.TASK.task(r);
        }
    }

    /**
     * Run task later (delay in ticks)
     * @param r
     * @param delay
     */
    public static void runTaskLater(final Runnable r, final int delay) {
        if (r != null) {
            PlotSquared.TASK.taskLater(r, delay);
        }
    }

    public static void runTaskLaterAsync(final Runnable r, final int delay) {
        if (r != null) {
            PlotSquared.TASK.taskLaterAsync(r, delay);
        }
    }
}
