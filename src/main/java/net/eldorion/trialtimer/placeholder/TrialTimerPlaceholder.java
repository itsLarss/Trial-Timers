package net.eldorion.trialtimer.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.eldorion.trialtimer.TrialTimerPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for TrialTimer.
 *
 * Available placeholders:
 *   %trialtimer_cooldown_minutes%  – configured cooldown in minutes
 *   %trialtimer_cooldown_seconds%  – configured cooldown in seconds
 *   %trialtimer_cooldown_ticks%    – configured cooldown in ticks
 */
public class TrialTimerPlaceholder extends PlaceholderExpansion {

    private final TrialTimerPlugin plugin;

    public TrialTimerPlaceholder(TrialTimerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "trialtimer"; }

    @Override
    public @NotNull String getAuthor() { return "EldorionMC"; }

    @Override
    public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "cooldown_minutes" -> String.valueOf(plugin.cfg().getCooldownMinutes());
            case "cooldown_seconds" -> String.format("%.1f", plugin.cfg().getCooldownMinutes() * 60.0);
            case "cooldown_ticks"   -> String.valueOf(plugin.cfg().getCooldownTicks());
            default -> null;
        };
    }
}
