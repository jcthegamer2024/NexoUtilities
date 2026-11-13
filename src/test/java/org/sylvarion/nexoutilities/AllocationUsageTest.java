package org.sylvarion.nexoutilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllocationUsageTest {
    @Test
    void showsEmptyPartialAndFullRegistries() {
        assertEquals(0, AllocationUsage.filledSegments(0, 1149));
        assertEquals(20, AllocationUsage.filledSegments(500, 1000));
        assertEquals(40, AllocationUsage.filledSegments(1149, 1149));
    }

    @Test
    void keepsSmallUsageVisibleAndAlmostFullDistinctFromFull() {
        assertEquals(1, AllocationUsage.filledSegments(1, 1149));
        assertEquals(39, AllocationUsage.filledSegments(1148, 1149));
    }

    @Test
    void rejectsInvalidCounts() {
        assertThrows(IllegalArgumentException.class, () -> AllocationUsage.filledSegments(0, 0));
        assertThrows(IllegalArgumentException.class, () -> AllocationUsage.filledSegments(-1, 100));
        assertThrows(IllegalArgumentException.class, () -> AllocationUsage.filledSegments(101, 100));
    }
}
