package org.sylvarion.nexoutilities;

import java.util.HashMap;
import java.util.Map;

final class DirectionalSlots {
    private final int first;
    private final int last;
    private final Map<Integer, String> occupied;

    DirectionalSlots(int first, int last, Map<Integer, String> occupied) {
        AllocationUsage.capacity(first, last);
        this.first = Math.max(1, first);
        this.last = last;
        this.occupied = new HashMap<>(occupied);
    }

    DirectionalSlots copy() {
        return new DirectionalSlots(first, last, occupied);
    }

    int reserve(String id, int requested) {
        if (requested != 0) {
            if (requested < first || requested > last) throw new IllegalArgumentException("Variation outside supported range: " + requested);
            String owner = occupied.get(requested);
            if (owner != null && !owner.equals(id)) {
                throw new IllegalArgumentException("Variation " + requested + " is already allocated to " + owner);
            }
            occupied.put(requested, id);
            return requested;
        }
        for (int slot = first; slot <= last; slot++) {
            if (!occupied.containsKey(slot)) {
                occupied.put(slot, id);
                return slot;
            }
        }
        throw new IllegalArgumentException("No free directional block slots remain");
    }
}
