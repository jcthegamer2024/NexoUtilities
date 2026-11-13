package org.sylvarion.nexoutilities;

import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

final class NoteBlocksCommand implements BasicCommand {
    private static final String PERMISSION = "nexoutils.command.noteblocks";
    private final JavaPlugin plugin;

    NoteBlocksCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String permission() {
        return PERMISSION;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to view block allocations.", NamedTextColor.RED));
            return;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("noteblocks")) {
            sender.sendMessage(Component.text("Usage: /nexoutils noteblocks", NamedTextColor.YELLOW));
            return;
        }
        try {
            // Fetch the factory on every invocation: Nexo replaces it during reloads.
            var factory = NoteBlockMechanicFactory.Companion.instance();
            if (factory == null) {
                sender.sendMessage(Component.text("Nexo note block mechanics are disabled or not ready yet.", NamedTextColor.YELLOW));
                return;
            }
            int capacity = NoteBlockMechanicFactory.Companion.getMAX_BLOCK_VARIATION();
            // Registry keys are unique variations, including generated directional variants.
            int used = (int) factory.getBLOCK_PER_VARIATION().keySet().stream()
                    .filter(variation -> variation >= 1 && variation <= capacity).count();
            sender.sendMessage(Component.text("Nexo note block slots", NamedTextColor.GOLD));
            sender.sendMessage(AllocationUsage.bar(used, capacity));
            sender.sendMessage(Component.text(String.format(Locale.ROOT,
                    "%d / %d used (%.1f%%) • %d free", used, capacity,
                    100.0 * used / capacity, capacity - used), NamedTextColor.WHITE));
            sender.sendMessage(Component.text("Allocated custom states, including directional variants.", NamedTextColor.GRAY));
        } catch (RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not read Nexo note block allocations", exception);
            sender.sendMessage(Component.text("Unable to read Nexo allocations. Check the server log for details.", NamedTextColor.RED));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return args.length == 0 || (args.length == 1 && "noteblocks".startsWith(args[0].toLowerCase(Locale.ROOT)))
                ? List.of("noteblocks") : List.of();
    }
}
