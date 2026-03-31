package net.eldorion.trialtimer;

import net.eldorion.trialtimer.commands.TrialTimerCommand;
import net.eldorion.trialtimer.config.ConfigManager;
import net.eldorion.trialtimer.lang.LanguageManager;
import net.eldorion.trialtimer.listener.TrialSpawnerListener;
import net.eldorion.trialtimer.placeholder.TrialTimerPlaceholder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public class TrialTimerPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private BukkitTask scanTask;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);

        startScanTask();

        TrialTimerCommand cmd = new TrialTimerCommand(this);
        getCommand("trialtimer").setExecutor(cmd);
        getCommand("trialtimer").setTabCompleter(cmd);

        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TrialTimerPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found – placeholders registered.");
        }

        getLogger().info(languageManager.get("plugin.enabled", Map.of(
                "version", getPluginMeta().getVersion(),
                "minutes", String.valueOf(configManager.getCooldownMinutes())
        )));
    }

    @Override
    public void onDisable() {
        if (scanTask != null) scanTask.cancel();
        getLogger().info(languageManager.get("plugin.disabled"));
    }

    public void reload() {
        configManager.reload();
        languageManager.reload();
        if (scanTask != null) scanTask.cancel();
        startScanTask();
    }

    private void startScanTask() {
        scanTask = new TrialSpawnerListener(this).runTaskTimer(this, 40L, 80L);
    }

    public ConfigManager cfg() { return configManager; }
    public LanguageManager lang() { return languageManager; }
}
