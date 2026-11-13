package org.sylvarion.nexoutilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllocationUsageTest {
    @Test
    void countsOnlyDistinctValidVariations() {
        assertEquals(3, AllocationUsage.count(java.util.stream.IntStream.of(-1, 0, 1, 1, 2, 63, 64), 1, 63));
        assertEquals(63, AllocationUsage.capacity(1, 63));
        assertEquals(127, AllocationUsage.capacity(0, 127));
        assertEquals(3, AllocationUsage.capacity(5, 7));
        assertEquals(1, AllocationUsage.count(java.util.stream.IntStream.of(0, 4, 5, 8), 5, 7));
    }

    @Test
    void countsEmptyTinyNearlyFullAndFullRanges() {
        assertEquals(0, AllocationUsage.count(java.util.stream.IntStream.empty(), 1, 63));
        assertEquals(1, AllocationUsage.count(java.util.stream.IntStream.of(1), 1, 1));
        assertEquals(62, AllocationUsage.count(java.util.stream.IntStream.range(1, 63), 1, 63));
        assertEquals(127, AllocationUsage.count(java.util.stream.IntStream.rangeClosed(1, 127), 1, 127));
        assertThrows(IllegalArgumentException.class, () -> AllocationUsage.capacity(1, 0));
        assertThrows(IllegalArgumentException.class, () -> AllocationUsage.capacity(-5, 0));
    }

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
