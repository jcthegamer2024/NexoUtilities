package org.sylvarion.nexoutilities;

import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionalTypeTest {
    @Test
    void logUsesClickedAxisAndBarrelUsesClickedFace() {
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN}) {
            assertEquals("y", DirectionalType.LOG.direction(face, 90, 60));
        }
        for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.WEST}) {
            assertEquals("x", DirectionalType.LOG.direction(face, 0, 0));
        }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH}) {
            assertEquals("z", DirectionalType.LOG.direction(face, 0, 0));
        }
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
                BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            assertEquals(face.name().toLowerCase(java.util.Locale.ROOT), DirectionalType.BARREL.direction(face, 0, 0));
        }
    }

    @Test
    void furnaceFacesPlayerAndNormalizesYaw() {
        assertEquals("north", DirectionalType.FURNACE.direction(BlockFace.UP, 0, 90));
        assertEquals("east", DirectionalType.FURNACE.direction(BlockFace.UP, 90, 0));
        assertEquals("south", DirectionalType.FURNACE.direction(BlockFace.UP, 180, 0));
        assertEquals("west", DirectionalType.FURNACE.direction(BlockFace.UP, -90, 0));
        assertEquals("north", DirectionalType.FURNACE.direction(BlockFace.UP, 720, 0));
        assertEquals("north", DirectionalType.FURNACE.direction(BlockFace.UP, 45, 0));
        assertEquals("east", DirectionalType.FURNACE.direction(BlockFace.UP, 46, 0));
        assertEquals("north", DirectionalType.FURNACE.direction(BlockFace.UP, -45, 0));
    }

    @Test
    void dropperSupportsVerticalPitchThresholds() {
        assertEquals("down", DirectionalType.DROPPER.direction(BlockFace.NORTH, 0, -45));
        assertEquals("up", DirectionalType.DROPPER.direction(BlockFace.NORTH, 0, 45));
        assertEquals("east", DirectionalType.DROPPER.direction(BlockFace.UP, 90, 44));
    }

    @Test
    void rotatesInheritedModelsButLeavesExplicitModelsUnrotated() {
        assertEquals(new DirectionalType.Rotation(90, 0), DirectionalType.LOG.rotation("x", false));
        assertEquals(new DirectionalType.Rotation(90, 90), DirectionalType.LOG.rotation("z", false));
        assertEquals(new DirectionalType.Rotation(90, 270), DirectionalType.BARREL.rotation("west", false));
        assertEquals(new DirectionalType.Rotation(0, 180), DirectionalType.FURNACE.rotation("south", false));
        assertEquals(new DirectionalType.Rotation(270, 0), DirectionalType.DROPPER.rotation("up", false));
        assertEquals(new DirectionalType.Rotation(90, 0), DirectionalType.DROPPER.rotation("down", false));
        for (DirectionalType type : DirectionalType.values()) {
            for (String direction : type.directions) {
                assertEquals(new DirectionalType.Rotation(0, 0), type.rotation(direction, true));
            }
        }
        assertThrows(IllegalArgumentException.class, () -> DirectionalType.LOG.rotation("north", false));
        assertThrows(IllegalArgumentException.class, () -> DirectionalType.FURNACE.direction(BlockFace.UP, Float.NaN, 0));
    }
}
