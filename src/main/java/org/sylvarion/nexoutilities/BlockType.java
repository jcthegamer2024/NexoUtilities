package org.sylvarion.nexoutilities;

import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.mechanics.custom_block.chorusblock.ChorusBlockFactory;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.IntStream;

@NullMarked
enum BlockType {
    NOTE("noteblocks", "noteblock", "note block"),
    CHORUS("chorusblocks", "chorusblock", "chorus block"),
    STRING("stringblocks", "stringblock", "string block");

    final String command;
    final String mechanic;
    final String label;

    BlockType(String command, String mechanic, String label) {
        this.command = command;
        this.mechanic = mechanic;
        this.label = label;
    }

    String permission() {
        return "nexoutils.command." + command;
    }

    @Nullable Usage readUsage() {
        // Resolve anew on each invocation because Nexo replaces factories during reloads.
        var factory = MechanicsManager.INSTANCE.mechanicFactory(mechanic);
        if (this == NOTE && factory instanceof NoteBlockMechanicFactory notes) {
            return usage(notes.getBLOCK_PER_VARIATION().keySet().intStream(),
                    1, NoteBlockMechanicFactory.Companion.getMAX_BLOCK_VARIATION());
        }
        if (this == CHORUS && factory instanceof ChorusBlockFactory chorus) {
            var range = ChorusBlockFactory.Companion.getMAX_BLOCK_VARIATION();
            return usage(chorus.getBLOCK_PER_VARIATION().keySet().intStream(), range.getFirst(), range.getLast());
        }
        if (this == STRING && factory instanceof StringBlockMechanicFactory strings) {
            var range = StringBlockMechanicFactory.Companion.getMAX_BLOCK_VARIATION();
            return usage(strings.getBLOCK_PER_VARIATION().keySet().intStream(), range.getFirst(), range.getLast());
        }
        return null;
    }

    private static Usage usage(IntStream keys, int first, int last) {
        return new Usage(AllocationUsage.count(keys, first, last), AllocationUsage.capacity(first, last));
    }

    record Usage(int used, int capacity) {}
}
