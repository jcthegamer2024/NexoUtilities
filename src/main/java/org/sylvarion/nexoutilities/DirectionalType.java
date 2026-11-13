package org.sylvarion.nexoutilities;

import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.Locale;

enum DirectionalType {
    LOG(List.of("y", "x", "z")),
    BARREL(List.of("up", "down", "north", "south", "east", "west")),
    FURNACE(List.of("north", "south", "east", "west")),
    DROPPER(List.of("north", "south", "east", "west", "up", "down"));

    final List<String> directions;

    DirectionalType(List<String> directions) {
        this.directions = directions;
    }

    String direction(BlockFace clickedFace, float yaw, float pitch) {
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("Invalid player rotation");
        }
        if (this == LOG) {
            return switch (clickedFace) {
                case EAST, WEST -> "x";
                case NORTH, SOUTH -> "z";
                default -> "y";
            };
        }
        if (this == BARREL) {
            String direction = clickedFace.name().toLowerCase(Locale.ROOT);
            if (!directions.contains(direction)) throw new IllegalArgumentException("Invalid placement face");
            return direction;
        }
        // Match Nexo's dropper pitch thresholds and face back towards the player.
        if (this == DROPPER && pitch <= -45) return "down";
        if (this == DROPPER && pitch >= 45) return "up";
        double normalized = ((double) yaw % 360 + 360) % 360;
        if (normalized <= 45 || normalized >= 315) return "north";
        if (normalized <= 135) return "east";
        if (normalized <= 225) return "south";
        return "west";
    }

    Rotation rotation(String direction, boolean explicitModel) {
        if (!directions.contains(direction)) throw new IllegalArgumentException("Invalid direction: " + direction);
        if (explicitModel) return new Rotation(0, 0);
        if (this == LOG) {
            return switch (direction) {
                case "x" -> new Rotation(90, 0);
                case "z" -> new Rotation(90, 90);
                default -> new Rotation(0, 0);
            };
        }
        int y = switch (direction) {
            case "east" -> 90;
            case "south" -> 180;
            case "west" -> 270;
            default -> 0;
        };
        int x = this == BARREL ? switch (direction) {
            case "up" -> 0;
            case "down" -> 180;
            default -> 90;
        } : switch (direction) {
            case "up" -> 270;
            case "down" -> 90;
            default -> 0;
        };
        return new Rotation(x, y);
    }

    record Rotation(int x, int y) {}
}
