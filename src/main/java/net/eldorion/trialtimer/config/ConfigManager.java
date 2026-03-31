package net.eldorion.trialtimer.config;

import net.eldorion.trialtimer.TrialTimerPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final TrialTimerPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(TrialTimerPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public double getCooldownMinutes() {
        return config.getDouble("cooldown-minutes", 5.0);
    }

    public long getCooldownTicks() {
        return Math.max(1L, (long) (getCooldownMinutes() * 60.0 * 20.0));
    }

    public String getLanguage() {
        return config.getString("language", "en").toLowerCase();
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public boolean isBroadcastEnabled() {
        return config.getBoolean("broadcast.enabled", true);
    }

    public double getBroadcastRadius() {
        return config.getDouble("broadcast.radius", 64.0);
    }
}
