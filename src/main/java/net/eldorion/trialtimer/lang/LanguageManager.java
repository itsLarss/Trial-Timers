package net.eldorion.trialtimer.lang;

import net.eldorion.trialtimer.TrialTimerPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LanguageManager {

    private final TrialTimerPlugin plugin;
    private FileConfiguration messages;
    private FileConfiguration fallback;

    public LanguageManager(TrialTimerPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        // Save both default lang files on first start
        saveDefaultIfAbsent("en");
        saveDefaultIfAbsent("de");

        String lang = plugin.cfg().getLanguage();
        File langFile = new File(plugin.getDataFolder(), "lang/messages_" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file 'messages_" + lang + ".yml' not found – falling back to English.");
            langFile = new File(plugin.getDataFolder(), "lang/messages_en.yml");
        }

        messages = YamlConfiguration.loadConfiguration(langFile);

        // Load bundled English as fallback so we never get a missing key error
        InputStream stream = plugin.getResource("lang/messages_en.yml");
        if (stream != null) {
            fallback = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private void saveDefaultIfAbsent(String lang) {
        File file = new File(plugin.getDataFolder(), "lang/messages_" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/messages_" + lang + ".yml", false);
        }
    }

    public void reload() {
        load();
    }

    /**
     * Returns a translated message, replacing any {placeholder} tokens.
     */
    public String get(String key, Map<String, String> placeholders) {
        String msg = messages.getString(key);
        if (msg == null && fallback != null) {
            msg = fallback.getString(key);
        }
        if (msg == null) {
            return "§c[TrialTimer] Missing key: " + key;
        }

        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        }

        return msg.replace("&", "§");
    }

    /** Shorthand without placeholders. */
    public String get(String key) {
        return get(key, Map.of());
    }
}
