package org.sylvarion.nexoutilities;

import org.bukkit.event.Cancellable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DeferredPlacementTest {
    @Test
    void rotatesAfterNexoRestoresItsParentState() {
        var event = new PlacementEvent();
        var state = new AtomicReference<>("parent");
        Runnable completion = DeferredPlacement.after(event, () -> state.get().equals("parent"),
                () -> state.set("east"));
        // Reproduce Nexo's write after its custom event handlers return.
        state.set("parent");
        assertEquals("parent", state.get());
        completion.run();
        assertEquals("east", state.get());
    }

    @Test
    void respectsCancellationByALaterListener() {
        var event = new PlacementEvent();
        var state = new AtomicReference<>("restored previous block");
        Runnable completion = DeferredPlacement.after(event, () -> true, () -> state.set("east"));
        event.setCancelled(true);
        completion.run();
        assertEquals("restored previous block", state.get());
    }

    @Test
    void leavesReplacedBlocksAndInvalidatedPlacementsAlone() {
        var event = new PlacementEvent();
        var state = new AtomicReference<>("parent");
        Runnable completion = DeferredPlacement.after(event, () -> state.get().equals("parent"),
                () -> state.set("east"));
        state.set("another block");
        completion.run();
        assertEquals("another block", state.get());
        // A reload or unloaded chunk also invalidates the guard without touching the block.
        state.set("parent");
        DeferredPlacement.after(event, () -> false, () -> state.set("east")).run();
        assertEquals("parent", state.get());
    }

    private static final class PlacementEvent implements Cancellable {
        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
