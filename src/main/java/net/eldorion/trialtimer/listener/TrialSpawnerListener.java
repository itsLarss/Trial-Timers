package net.eldorion.trialtimer.listener;

import net.eldorion.trialtimer.TrialTimerPlugin;
import net.eldorion.trialtimer.util.NMSUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialSpawnerListener extends BukkitRunnable {

    private final TrialTimerPlugin plugin;
    private int tickCount = 0;

    /**
     * Tracks spawners currently on cooldown.
     * Key: "world:x:y:z"  Value: estimated game-tick when cooldown ends
     */
    private final Map<String, Long> cooldownTracker = new HashMap<>();

    public TrialSpawnerListener(TrialTimerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long maxCooldownTicks = plugin.cfg().getCooldownTicks();
        boolean debug = plugin.cfg().isDebug();
        int totalChunks = 0;
        int foundSpawners = 0;

        for (World world : plugin.getServer().getWorlds()) {
            Chunk[] loaded = world.getLoadedChunks();
            totalChunks += loaded.length;

            for (Chunk chunk : loaded) {
                List<Block> spawners = getTrialSpawnersInChunk(chunk);
                foundSpawners += spawners.size();

                for (Block block : spawners) {
                    try {
                        long[] result = NMSUtil.setCooldownIfLonger(block, maxCooldownTicks, debug, plugin.getLogger());
                        // result[0] = 1 if patched, 0 otherwise
                        // result[1] = cooldownEndsAt after operation (0 if spawner not in cooldown)

                        boolean patched = result[0] == 1;
                        long cooldownEndsAt = result[1];
                        String key = blockKey(block);

                        if (patched && debug) {
                            plugin.getLogger().info("[Debug] Patched spawner at " +
                                    block.getX() + "," + block.getY() + "," + block.getZ());
                        }

                        if (cooldownEndsAt > 0) {
                            // Spawner is in cooldown — track it
                            cooldownTracker.put(key, cooldownEndsAt);
                        } else if (cooldownTracker.containsKey(key)) {
                            // Was tracked but cooldown ended → broadcast
                            cooldownTracker.remove(key);
                            if (plugin.cfg().isBroadcastEnabled()) {
                                broadcastReady(block);
                            }
                        }

                    } catch (Throwable t) {
                        plugin.getLogger().severe("[TrialTimer] ERROR: " +
                                t.getClass().getName() + ": " + t.getMessage());
                    }
                }
            }
        }

        if (debug && ++tickCount % 10 == 0) {
            plugin.getLogger().info("[Debug] Scan #" + tickCount +
                    " | chunks=" + totalChunks +
                    " | trial_spawners_found=" + foundSpawners +
                    " | tracked_on_cooldown=" + cooldownTracker.size());
        }
    }

    private void broadcastReady(Block block) {
        Location loc = block.getLocation();
        double radius = plugin.cfg().getBroadcastRadius();
        double radiusSq = radius * radius;

        Map<String, String> placeholders = Map.of(
                "x", String.valueOf(block.getX()),
                "y", String.valueOf(block.getY()),
                "z", String.valueOf(block.getZ())
        );
        String message = plugin.lang().get("broadcast.ready", placeholders);

        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= radiusSq) {
                player.sendMessage(message);
            }
        }
    }

    private String blockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    private List<Block> getTrialSpawnersInChunk(Chunk chunk) {
        List<Block> result = new ArrayList<>();
        try {
            for (var tileState : chunk.getTileEntities()) {
                if (tileState.getType() == Material.TRIAL_SPAWNER) {
                    result.add(tileState.getBlock());
                }
            }
        } catch (Throwable e) {
            plugin.getLogger().warning("[TrialTimer] getTileEntities error: " + e.getMessage());
        }
        return result;
    }
}
