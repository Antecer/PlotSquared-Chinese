package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;

public class Chat extends SubCommand {
    public Chat() {
        super(Command.CHAT, "切换地皮聊天开关", "chat", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(PlotPlayer plr, String... args) {
        final String world = plr.getLocation().getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        boolean enable = !(plr.getMeta("chat") != null && (Boolean) plr.getMeta("chat"));
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("on")) {
                enable = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                enable = false;
            }
        }
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if (!enable && plotworld.PLOT_CHAT) {
            return !sendMessage(plr, C.PLOT_CHAT_FORCED);
        }
        plr.setMeta("chat", enable);
        return sendMessage(plr, enable ? C.PLOT_CHAT_ON : C.PLOT_CHAT_OFF);
    }
}
