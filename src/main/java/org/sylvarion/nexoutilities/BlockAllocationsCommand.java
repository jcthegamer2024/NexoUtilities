package org.sylvarion.nexoutilities;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

@NullMarked
final class BlockAllocationsCommand implements BasicCommand {
    private final JavaPlugin plugin;

    BlockAllocationsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return Arrays.stream(BlockType.values()).anyMatch(type -> sender.hasPermission(type.permission()));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        var type = args.length == 1 ? Arrays.stream(BlockType.values())
                .filter(candidate -> candidate.command.equalsIgnoreCase(args[0])).findFirst().orElse(null) : null;
        if (type == null) {
            sender.sendMessage(Component.text("Usage: /nexoutils <noteblocks|chorusblocks|stringblocks>", NamedTextColor.YELLOW));
            return;
        }
        if (!sender.hasPermission(type.permission())) {
            sender.sendMessage(Component.text("You do not have permission to view block allocations.", NamedTextColor.RED));
            return;
        }
        try {
            var usage = type.readUsage();
            if (usage == null) {
                sender.sendMessage(Component.text("Nexo " + type.label + " mechanics are disabled or not ready yet.", NamedTextColor.YELLOW));
                return;
            }
            int used = usage.used();
            int capacity = usage.capacity();
            sender.sendMessage(Component.text("Nexo " + type.label + " slots", NamedTextColor.GOLD));
            sender.sendMessage(AllocationUsage.bar(used, capacity));
            sender.sendMessage(Component.text(String.format(Locale.ROOT,
                    "%d / %d used (%.1f%%) • %d free", used, capacity,
                    100.0 * used / capacity, capacity - used), NamedTextColor.WHITE));
            sender.sendMessage(Component.text("Allocated custom states, including directional variants where registered.", NamedTextColor.GRAY));
        } catch (RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not read Nexo " + type.label + " allocations", exception);
            sender.sendMessage(Component.text("Unable to read Nexo allocations. Check the server log for details.", NamedTextColor.RED));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length > 1) return List.of();
        String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        return Arrays.stream(BlockType.values())
                .filter(type -> source.getSender().hasPermission(type.permission()))
                .map(type -> type.command).filter(command -> command.startsWith(prefix)).toList();
    }
}
