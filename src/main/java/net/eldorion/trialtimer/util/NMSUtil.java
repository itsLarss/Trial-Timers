package net.eldorion.trialtimer.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public final class NMSUtil {

    private NMSUtil() {}

    /**
     * Returns long[2]:
     *   [0] = 1 if the cooldown was patched, 0 otherwise
     *   [1] = current cooldownEndsAt value (0 if spawner not in cooldown)
     */
    public static long[] setCooldownIfLonger(Block block, long maxCooldownTicks, boolean debug, Logger logger) {
        try {
            Method getHandle = block.getWorld().getClass().getMethod("getHandle");
            Object serverLevel = getHandle.invoke(block.getWorld());

            Class<?> blockPosClass = Class.forName("net.minecraft.core.BlockPos");
            Object pos = blockPosClass
                    .getConstructor(int.class, int.class, int.class)
                    .newInstance(block.getX(), block.getY(), block.getZ());

            Method getBlockEntity = serverLevel.getClass().getMethod("getBlockEntity", blockPosClass);
            Object blockEntity = getBlockEntity.invoke(serverLevel, pos);

            if (blockEntity == null) return new long[]{0, 0};
            if (!blockEntity.getClass().getName().contains("TrialSpawner")) return new long[]{0, 0};

            Method getTrialSpawner = blockEntity.getClass().getMethod("getTrialSpawner");
            Object trialSpawner = getTrialSpawner.invoke(blockEntity);

            Field dataField = findField(trialSpawner.getClass(), "data");
            dataField.setAccessible(true);
            Object trialSpawnerData = dataField.get(trialSpawner);

            Method getGameTime = serverLevel.getClass().getMethod("getGameTime");
            long gameTime = (long) getGameTime.invoke(serverLevel);

            Field cooldownField = findFieldAny(trialSpawnerData.getClass(), "cooldownEndsAt", "nextEventTime");
            cooldownField.setAccessible(true);
            long cooldownEndsAt = cooldownField.getLong(trialSpawnerData);
            long remainingTicks = cooldownEndsAt - gameTime;

            if (debug) logger.info("[Debug] gameTime=" + gameTime + " cooldownEndsAt=" + cooldownEndsAt
                    + " remaining=" + remainingTicks + " max=" + maxCooldownTicks);

            // Not in cooldown
            if (remainingTicks <= 0) return new long[]{0, 0};

            // Already within limit
            if (remainingTicks <= maxCooldownTicks) return new long[]{0, cooldownEndsAt};

            // Patch
            long newEndsAt = gameTime + maxCooldownTicks;
            cooldownField.setLong(trialSpawnerData, newEndsAt);
            blockEntity.getClass().getMethod("setChanged").invoke(blockEntity);
            notifyPlayers(block);

            return new long[]{1, newEndsAt};

        } catch (Throwable e) {
            if (debug) logger.warning("[Debug] NMS exception: " + e.getClass().getName() + ": " + e.getMessage());
            return new long[]{0, 0};
        }
    }

    private static void notifyPlayers(Block block) {
        org.bukkit.Location loc = block.getLocation();
        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= 64 * 64) {
                player.sendBlockUpdate(loc, (org.bukkit.block.TileState) block.getState());
            }
        }
    }

    private static Field findFieldAny(Class<?> clazz, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try { return findField(clazz, name); } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException("None of " + java.util.Arrays.toString(names) + " found in " + clazz.getName());
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try { return current.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) { current = current.getSuperclass(); }
        }
        throw new NoSuchFieldException("'" + name + "' not found in " + clazz.getName());
    }
}
