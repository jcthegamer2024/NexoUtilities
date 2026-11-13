package org.sylvarion.nexoutilities;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
final class DirectionalPickBlock {
    private DirectionalPickBlock() {}

    static void pick(PlayerPickBlockEvent event, String parent,
                     Function<ItemStack, @Nullable String> idOf, Supplier<@Nullable ItemStack> create) {
        if (event.isCancelled()) return;
        // Handle this before Nexo's HIGH-priority listener selects a direction-specific item.
        event.setCancelled(true);
        var player = event.getPlayer();
        var inventory = player.getInventory();
        int target = event.getTargetSlot();
        for (int slot = 0; slot < 36; slot++) {
            var item = inventory.getItem(slot);
            if (item == null || !parent.equals(idOf.apply(item))) continue;
            if (slot < 9) {
                inventory.setHeldItemSlot(slot);
            } else {
                var displaced = inventory.getItem(target);
                inventory.setItem(target, item);
                inventory.setItem(slot, displaced);
                inventory.setHeldItemSlot(target);
            }
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            var item = create.get();
            if (item != null) {
                inventory.setItem(target, item);
                inventory.setHeldItemSlot(target);
            }
        }
    }
}
