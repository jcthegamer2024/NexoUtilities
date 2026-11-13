package org.sylvarion.nexoutilities;

import org.bukkit.event.Cancellable;

import java.util.function.BooleanSupplier;

final class DeferredPlacement {
    private DeferredPlacement() {}

    static Runnable after(Cancellable event, BooleanSupplier stillOriginal, Runnable rotate) {
        // Nexo writes its original state after dispatching the custom placement event.
        // Read cancellation when the task executes, after all event listeners finish.
        return () -> {
            if (!event.isCancelled() && stillOriginal.getAsBoolean()) rotate.run();
        };
    }
}
