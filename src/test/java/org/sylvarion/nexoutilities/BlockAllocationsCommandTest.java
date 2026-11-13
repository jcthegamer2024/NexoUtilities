package org.sylvarion.nexoutilities;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockAllocationsCommandTest {
    private final JavaPlugin plugin = mock(JavaPlugin.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final CommandSourceStack source = mock(CommandSourceStack.class);
    private final List<BlockType> reads = new ArrayList<>();

    private BlockAllocationsCommand command() {
        when(source.getSender()).thenReturn(sender);
        return new BlockAllocationsCommand(plugin, type -> {
            reads.add(type);
            return new BlockType.Usage(1, 63);
        });
    }

    @Test
    void allBlocksPermissionAloneAllowsAllThreeReportsInOrder() {
        var command = command();
        when(sender.hasPermission("nexoutils.command.allblocks")).thenReturn(true);
        assertTrue(command.canUse(sender));
        command.execute(source, new String[]{"ALLBLOCKS"});
        assertEquals(List.of(BlockType.NOTE, BlockType.CHORUS, BlockType.STRING), reads);
        for (BlockType type : BlockType.values()) {
            verify(sender).sendMessage(Component.text("Nexo " + type.label + " slots", NamedTextColor.GOLD));
        }
    }

    @Test
    void individualPermissionDoesNotGrantCombinedReport() {
        var command = command();
        when(sender.hasPermission("nexoutils.command.noteblocks")).thenReturn(true);
        assertTrue(command.canUse(sender));
        command.execute(source, new String[]{"allblocks"});
        assertTrue(reads.isEmpty());
        verify(sender).sendMessage(Component.text("You do not have permission to view block allocations.", NamedTextColor.RED));
    }

    @Test
    void combinedPermissionDoesNotUnlockIndividualCommands() {
        var command = command();
        when(sender.hasPermission("nexoutils.command.allblocks")).thenReturn(true);
        command.execute(source, new String[]{"noteblocks"});
        assertTrue(reads.isEmpty());
    }

    @Test
    void completionOnlyOffersPermittedCommandsAndFiltersPrefixes() {
        var command = command();
        assertFalse(command.canUse(sender));
        assertTrue(command.suggest(source, new String[0]).isEmpty());
        when(sender.hasPermission("nexoutils.command.allblocks")).thenReturn(true);
        assertEquals(List.of("allblocks"), command.suggest(source, new String[]{"ALL"}));
        assertTrue(command.suggest(source, new String[]{"note"}).isEmpty());
        assertTrue(command.suggest(source, new String[]{"allblocks", "extra"}).isEmpty());
    }

    @Test
    void disabledAndFailingMechanicsDoNotPreventLaterReports() {
        when(source.getSender()).thenReturn(sender);
        when(sender.hasPermission("nexoutils.command.allblocks")).thenReturn(true);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        var command = new BlockAllocationsCommand(plugin, type -> {
            reads.add(type);
            if (type == BlockType.NOTE) return null;
            if (type == BlockType.CHORUS) throw new IllegalStateException("Unavailable registry");
            return new BlockType.Usage(2, 127);
        });
        command.execute(source, new String[]{"allblocks"});
        assertEquals(List.of(BlockType.NOTE, BlockType.CHORUS, BlockType.STRING), reads);
        verify(sender).sendMessage(Component.text("Nexo note block mechanics are disabled or not ready yet.", NamedTextColor.YELLOW));
        verify(sender).sendMessage(Component.text("Unable to read Nexo chorus block allocations. Check the server log for details.", NamedTextColor.RED));
        verify(sender).sendMessage(Component.text("Nexo string block slots", NamedTextColor.GOLD));
    }

    @Test
    void individualReportsRemainAvailableAndExtraArgumentsAreRejected() {
        var command = command();
        when(sender.hasPermission("nexoutils.command.chorusblocks")).thenReturn(true);
        command.execute(source, new String[]{"chorusblocks"});
        assertEquals(List.of(BlockType.CHORUS), reads);
        reads.clear();
        command.execute(source, new String[]{"allblocks", "extra"});
        assertTrue(reads.isEmpty());
    }
}
