package org.sylvarion.nexoutilities;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class DirectionalPickBlockTest {
    private final Player player = mock(Player.class);
    private final PlayerInventory inventory = mock(PlayerInventory.class);
    private final PlayerPickBlockEvent event = mock(PlayerPickBlockEvent.class);

    private void setup(GameMode mode) {
        when(event.getPlayer()).thenReturn(player);
        when(event.getTargetSlot()).thenReturn(2);
        when(player.getInventory()).thenReturn(inventory);
        when(player.getGameMode()).thenReturn(mode);
    }

    @Test
    void creativeCreatesBaseItemInsteadOfHiddenVariant() {
        setup(GameMode.CREATIVE);
        var base = mock(ItemStack.class);
        DirectionalPickBlock.pick(event, "base", item -> "hidden-east", () -> base);
        verify(event).setCancelled(true);
        verify(inventory).setItem(2, base);
        verify(inventory).setHeldItemSlot(2);
    }

    @Test
    void selectsExistingBaseHotbarStackWithoutReplacingIt() {
        setup(GameMode.CREATIVE);
        var base = mock(ItemStack.class);
        when(inventory.getItem(5)).thenReturn(base);
        DirectionalPickBlock.pick(event, "base", item -> "base", () -> { throw new AssertionError("Must not create another stack"); });
        verify(inventory).setHeldItemSlot(5);
        verify(inventory, never()).setItem(anyInt(), any());
    }

    @Test
    void survivalSwapsExistingBaseStackFromStorageAndPreservesDisplacedStack() {
        setup(GameMode.SURVIVAL);
        var base = mock(ItemStack.class);
        var displaced = mock(ItemStack.class);
        when(inventory.getItem(14)).thenReturn(base);
        when(inventory.getItem(2)).thenReturn(displaced);
        DirectionalPickBlock.pick(event, "base", item -> item == base ? "base" : "other",
                () -> { throw new AssertionError("Survival must not create items"); });
        verify(inventory).setItem(2, base);
        verify(inventory).setItem(14, displaced);
        verify(inventory).setHeldItemSlot(2);
    }

    @Test
    void survivalWithoutBaseDoesNotCreateOrReplaceItems() {
        setup(GameMode.SURVIVAL);
        DirectionalPickBlock.pick(event, "base", item -> "hidden-east",
                () -> { throw new AssertionError("Survival must not create items"); });
        verify(event).setCancelled(true);
        verify(inventory, never()).setItem(anyInt(), any());
        verify(inventory, never()).setHeldItemSlot(anyInt());
    }

    @Test
    void cancelledPickIsUntouched() {
        when(event.isCancelled()).thenReturn(true);
        DirectionalPickBlock.pick(event, "base", item -> "base",
                () -> { throw new AssertionError("Cancelled pick must not create items"); });
        verifyNoInteractions(player, inventory);
        verify(event, never()).setCancelled(anyBoolean());
    }
}
