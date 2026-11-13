package org.sylvarion.nexoutilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

final class AllocationUsage {
    static final int BAR_WIDTH = 40;

    private AllocationUsage() {}

    static Component bar(int used, int capacity) {
        int filled = filledSegments(used, capacity);
        double ratio = (double) used / capacity;
        var colour = ratio >= 0.9 ? NamedTextColor.RED
                : ratio >= 0.75 ? NamedTextColor.YELLOW : NamedTextColor.GREEN;
        return Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("|".repeat(filled), colour))
                .append(Component.text("|".repeat(BAR_WIDTH - filled), NamedTextColor.DARK_GRAY))
                .append(Component.text("]", NamedTextColor.GRAY));
    }

    static int filledSegments(int used, int capacity) {
        if (capacity <= 0 || used < 0 || used > capacity) {
            throw new IllegalArgumentException("Invalid slot usage: " + used + "/" + capacity);
        }
        // A non-empty registry remains visible; only a full registry fills the bar.
        if (used == 0) return 0;
        if (used == capacity) return BAR_WIDTH;
        return Math.max(1, Math.min(BAR_WIDTH - 1, (int) Math.round((double) used / capacity * BAR_WIDTH)));
    }
}
