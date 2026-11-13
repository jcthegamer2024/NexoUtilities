package org.sylvarion.nexoutilities;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DirectionalSlotsTest {
    @Test
    void reservesDistinctFreeSlotsWithoutUsingZero() {
        var slots = new DirectionalSlots(0, 3, Map.of(2, "existing"));
        assertEquals(1, slots.reserve("first", 0));
        assertEquals(3, slots.reserve("second", 0));
        assertThrows(IllegalArgumentException.class, () -> slots.reserve("full", 0));
    }

    @Test
    void preservesExplicitAndPersistedAllocations() {
        var slots = new DirectionalSlots(1, 63, Map.of(10, "saved"));
        assertEquals(10, slots.reserve("saved", 10));
        assertEquals(63, slots.reserve("explicit", 63));
        assertThrows(IllegalArgumentException.class, () -> slots.reserve("collision", 10));
        assertThrows(IllegalArgumentException.class, () -> slots.reserve("invalid", -1));
        assertThrows(IllegalArgumentException.class, () -> slots.reserve("invalid", 64));
    }

    @Test
    void failedFamilyPlansDoNotConsumeSlots() {
        var original = new DirectionalSlots(1, 1, Map.of());
        var candidate = original.copy();
        assertEquals(1, candidate.reserve("candidate", 0));
        assertThrows(IllegalArgumentException.class, () -> candidate.reserve("overflow", 0));
        assertEquals(1, original.reserve("other-family", 0));
        assertThrows(IllegalArgumentException.class, () -> new DirectionalSlots(1, 0, Map.of()));
    }
}
