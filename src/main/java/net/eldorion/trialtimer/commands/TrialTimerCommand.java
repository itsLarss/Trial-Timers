package net.eldorion.trialtimer.commands;

import net.eldorion.trialtimer.TrialTimerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public class TrialTimerCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "info", "help");

    private final TrialTimerPlugin plugin;

    public TrialTimerCommand(TrialTimerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("trialtimer.reload")) {
                    sender.sendMessage(plugin.lang().get("command.no-permission"));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(plugin.lang().get("command.reload-success"));
            }

            case "info" -> {
                if (!sender.hasPermission("trialtimer.use")) {
                    sender.sendMessage(plugin.lang().get("command.no-permission"));
                    return true;
                }
                double minutes = plugin.cfg().getCooldownMinutes();
                long ticks = plugin.cfg().getCooldownTicks();
                String seconds = String.format("%.1f", minutes * 60.0);
                sender.sendMessage(plugin.lang().get("command.info", Map.of(
                        "minutes", String.valueOf(minutes),
                        "seconds", seconds,
                        "ticks",   String.valueOf(ticks)
                )));
            }

            default -> sendHelp(sender, label);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(plugin.lang().get("command.help", Map.of("label", label)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
