package org.sylvarion.nexoutilities;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.chorusblock.ChorusBlockFactory;
import com.nexomc.nexo.mechanics.custom_block.chorusblock.ChorusBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@NullMarked
final class DirectionalBlocks implements Listener {
    private final JavaPlugin plugin;
    private final boolean enabled;
    private final Path file;
    private final YamlConfiguration saved = new YamlConfiguration();
    private volatile Map<String, Family> families = Map.of();
    private volatile DirectionalParents pickParents = new DirectionalParents(Map.of());
    private final Map<UUID, Placement> placements = new HashMap<>();

    DirectionalBlocks(JavaPlugin plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("modules.directional-blocks.enabled", false);
        file = plugin.getDataFolder().toPath().resolve("directional-variants.yml");
    }

    synchronized void start() throws Exception {
        // Strict load: never replace unreadable saved allocations with fresh numbers.
        if (Files.exists(file)) saved.load(file.toFile());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (String id : saved.getKeys(false)) {
            var section = saved.getConfigurationSection(id);
            if (section == null || section.getString(DirectionalConfigs.PARENT) == null) {
                throw new IllegalArgumentException("Invalid saved directional variant: " + id);
            }
            register(section);
        }
        if (enabled) plugin.getLogger().info("Directional chorus/string module enabled. Run /nexo reload all after configuration changes.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public synchronized void onItemsLoaded(NexoItemsLoadedEvent event) {
        families = Map.of();
        pickParents = new DirectionalParents(Map.of());
        try {
            if (enabled) refresh();
        } catch (RuntimeException | LinkageError exception) {
            families = Map.of();
            plugin.getLogger().log(Level.SEVERE, "Could not prepare directional chorus/string blocks; directional placement disabled", exception);
        }
        bindDrops();
    }

    private void refresh() {
        Map<String, Family> active = new HashMap<>();
        for (String mechanic : List.of("chorusblock", "stringblock")) {
            var factory = MechanicsManager.INSTANCE.mechanicFactory(mechanic);
            Map<Integer, String> occupied = new HashMap<>();
            DirectionalSlots slots;
            List<CustomBlockMechanic> blocks = new ArrayList<>();
            if (factory instanceof ChorusBlockFactory chorus) {
                chorus.getBLOCK_PER_VARIATION().forEach((slot, block) -> occupied.put(slot, block.getItemID()));
                blocks.addAll(chorus.getBLOCK_PER_VARIATION().values());
                var range = ChorusBlockFactory.Companion.getMAX_BLOCK_VARIATION();
                slots = new DirectionalSlots(range.getFirst(), range.getLast(), occupied);
            } else if (factory instanceof StringBlockMechanicFactory strings) {
                strings.getBLOCK_PER_VARIATION().forEach((slot, block) -> occupied.put(slot, block.getItemID()));
                blocks.addAll(strings.getBLOCK_PER_VARIATION().values());
                var range = StringBlockMechanicFactory.Companion.getMAX_BLOCK_VARIATION();
                slots = new DirectionalSlots(range.getFirst(), range.getLast(), occupied);
            } else continue;

            // Include retained allocations even if Nexo could not register one this load.
            for (String id : saved.getKeys(false)) {
                if (saved.getString(id + "." + DirectionalConfigs.BLOCK + ".type", "").equalsIgnoreCase(mechanic)) {
                    int variation = saved.getInt(id + "." + DirectionalConfigs.BLOCK + ".custom_variation");
                    if (variation <= 0) throw new IllegalArgumentException("Invalid persisted variation for " + id);
                    slots.reserve(id, variation);
                }
            }
            for (CustomBlockMechanic block : blocks.stream().distinct()
                    .sorted(java.util.Comparator.comparing(CustomBlockMechanic::getItemID)).toList()) {
                if (block.getItemID().startsWith("nexoutils_directional_")) continue;
                var directional = block.getSection().getConfigurationSection("directional");
                if (directional == null) continue;
                try {
                    var candidateSlots = slots.copy();
                    FamilyPlan plan = plan(block, mechanic, directional, candidateSlots);
                    // Persist the whole family before exposing any new state to placement.
                    for (var child : plan.children()) {
                        saved.set(child.getName(), null);
                        DirectionalConfigs.copy(child, saved.createSection(child.getName()));
                    }
                    persist();
                    slots = candidateSlots;
                    boolean registered = true;
                    for (var child : plan.children()) registered &= register(saved.getConfigurationSection(child.getName()));
                    if (!registered) throw new IllegalArgumentException("Nexo refused a generated variant; check its log");
                    active.put(block.getItemID(), plan.family());
                } catch (IllegalStateException exception) {
                    throw exception; // A persistence failure must stop all further allocation.
                } catch (RuntimeException exception) {
                    plugin.getLogger().log(Level.WARNING, "Skipping directional " + mechanic + " item " + block.getItemID(), exception);
                }
            }
        }
        families = Map.copyOf(active);
        Map<String, java.util.Collection<String>> references = new HashMap<>();
        active.forEach((parent, family) -> references.put(parent, family.directions().values()));
        pickParents = new DirectionalParents(references);
        plugin.getLogger().info("Prepared " + active.size() + " directional chorus/string block definitions.");
    }

    @SuppressWarnings("PatternValidation") // Dynamic model keys are validated with Key.key before saving.
    private FamilyPlan plan(CustomBlockMechanic parent, String mechanic, ConfigurationSection config, DirectionalSlots slots) {
        String parentId = parent.getItemID();
        if (parent instanceof StringBlockMechanic strings
                && (strings.getTall() || strings.isStackable() || strings.hasRandomPlace())) {
            throw new IllegalArgumentException("Directional string blocks cannot also be tall, stackable or random-place blocks");
        }
        if (!parentId.matches("[a-zA-Z0-9_:-]+")) throw new IllegalArgumentException("Unsupported directional item ID: " + parentId);
        DirectionalType type = DirectionalType.valueOf(config.getString("type", "LOG").toUpperCase(Locale.ROOT));
        Map<String, String> directions = new HashMap<>();
        List<ConfigurationSection> children = new ArrayList<>();
        for (String direction : type.directions) {
            String reference = config.getString(direction + "_block");
            if (reference != null) {
                var target = NexoBlocks.customBlockMechanic(reference);
                if (!sameType(parent, target)) throw new IllegalArgumentException("Invalid " + direction + "_block: " + reference);
                directions.put(direction, reference);
                continue;
            }
            String model = config.getString(direction + "_model");
            var rotation = type.rotation(direction, model != null);
            if (model == null) model = parent.getModel().asString();
            net.kyori.adventure.key.Key.key(model); // Reject malformed keys before saving anything.
            if (config.contains(direction + "_variation") && !config.isInt(direction + "_variation")) {
                throw new IllegalArgumentException(direction + "_variation must be an integer");
            }
            int requested = config.getInt(direction + "_variation");
            if (config.contains(direction + "_variation") && requested <= 0) {
                throw new IllegalArgumentException(direction + "_variation must be positive");
            }
            if (requested == parent.getCustomVariation() && rotation.x() == 0 && rotation.y() == 0
                    && model.equals(parent.getModel().asString())) {
                directions.put(direction, parentId);
                continue;
            }
            String id = "nexoutils_directional_" + mechanic + "_" + parentId + "_" + direction;
            if (NexoItems.exists(id) && !saved.isConfigurationSection(id)) {
                throw new IllegalArgumentException("Generated item ID already exists: " + id);
            }
            int previous = saved.getInt(id + "." + DirectionalConfigs.BLOCK + ".custom_variation");
            if (previous > 0 && requested > 0 && requested != previous) {
                throw new IllegalArgumentException("Cannot renumber persisted variant " + id + "; its slot is " + previous);
            }
            int variation = slots.reserve(id, previous > 0 ? previous : requested);
            children.add(DirectionalConfigs.child(id, parentId, parent.getSection(), variation, model, rotation));
            directions.put(direction, id);
        }
        return new FamilyPlan(new Family(type, Map.copyOf(directions)), children);
    }

    private static boolean sameType(CustomBlockMechanic parent, @Nullable CustomBlockMechanic target) {
        return parent instanceof ChorusBlockMechanic && target instanceof ChorusBlockMechanic
                || parent instanceof StringBlockMechanic && target instanceof StringBlockMechanic;
    }

    private boolean register(@Nullable ConfigurationSection section) {
        if (section == null) return false;
        String parent = section.getString(DirectionalConfigs.PARENT);
        if (parent == null) throw new IllegalArgumentException("Missing parent for " + section.getName());
        String variationPath = DirectionalConfigs.BLOCK + ".custom_variation";
        int expectedVariation = section.getInt(variationPath);
        String type = section.getString(DirectionalConfigs.BLOCK + ".type", "").toLowerCase(Locale.ROOT);
        var factory = MechanicsManager.INSTANCE.mechanicFactory(type);
        CustomBlockMechanic owner = factory instanceof ChorusBlockFactory chorus
                ? chorus.getBLOCK_PER_VARIATION().get(expectedVariation)
                : factory instanceof StringBlockMechanicFactory strings
                ? strings.getBLOCK_PER_VARIATION().get(expectedVariation) : null;
        if (expectedVariation <= 0 || owner != null && !owner.getItemID().equals(section.getName())) {
            throw new IllegalArgumentException("Cannot register saved variant " + section.getName()
                    + ": variation " + expectedVariation + " is invalid or occupied");
        }
        boolean registered = NexoItems.registerExternalItem(plugin, section, updated -> {
            synchronized (this) {
                if (updated.getInt(variationPath) != expectedVariation) {
                    updated.set(variationPath, expectedVariation);
                    throw new IllegalStateException("Nexo attempted to renumber saved variant " + updated.getName());
                }
                if (updated != saved.getConfigurationSection(updated.getName())) {
                    saved.set(updated.getName(), null);
                    DirectionalConfigs.copy(updated, saved.createSection(updated.getName()));
                }
                persist();
            }
        }, () -> {
            ItemStack item = NexoItems.createItem(parent);
            return item != null ? item : new ItemStack(Material.PAPER);
        }) != null;
        if (registered) bindDrop(section.getName());
        return registered;
    }

    private void bindDrops() {
        for (String id : saved.getKeys(false)) {
            bindDrop(id);
        }
    }

    private void bindDrop(String id) {
        var child = NexoBlocks.customBlockMechanic(id);
        var parent = NexoBlocks.customBlockMechanic(saved.getString(id + "." + DirectionalConfigs.PARENT, ""));
        if (child != null && parent != null && sameType(child, parent)) {
            // Preserve parent loot, tool requirements, silk touch and fortune rules.
            child.getBreakable().setDrop(parent.getBreakable().getDrop());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(file.getParent());
            Path temporary = Files.createTempFile(file.getParent(), "directional-", ".tmp");
            try {
                Files.writeString(temporary, saved.saveToString(), StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not persist directional allocation identities to " + file, exception);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPickBlock(PlayerPickBlockEvent event) {
        if (!enabled) return;
        var mechanic = NexoBlocks.customBlockMechanic(event.getBlock());
        if (mechanic == null) return;
        String parent = pickParents.parent(mechanic.getItemID());
        if (parent == null) return;
        DirectionalPickBlock.pick(event, parent, NexoItems::idFromItem, () -> NexoItems.createItem(parent));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!enabled || !event.canBuild()) return;
        String id = NexoItems.idFromItem(event.getItemInHand());
        if (id == null || !families.containsKey(id)) return;
        BlockFace face = event.getBlockAgainst().getFace(event.getBlockPlaced());
        if (face == null) return;
        UUID player = event.getPlayer().getUniqueId();
        Placement placement = new Placement(event.getBlockPlaced().getLocation(), face);
        placements.put(player, placement);
        plugin.getServer().getScheduler().runTask(plugin, () -> placements.remove(player, placement));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNexoPlace(NexoBlockPlaceEvent event) {
        if (!enabled) return;
        Placement placement = placements.remove(event.getPlayer().getUniqueId());
        Family family = families.get(event.getMechanic().getItemID());
        if (family == null) return;
        if (placement == null || !placement.location().equals(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        var location = event.getPlayer().getLocation();
        String direction = family.type().direction(placement.face(), location.getYaw(), location.getPitch());
        var target = NexoBlocks.customBlockMechanic(family.directions().get(direction));
        var data = target == null || !sameType(event.getMechanic(), target) ? null : target.getBlockData();
        if (data == null) {
            event.setCancelled(true);
            plugin.getLogger().warning("Missing directional state for " + event.getMechanic().getItemID() + ": " + direction);
            return;
        }
        String parentId = event.getMechanic().getItemID();
        var block = event.getBlock();
        plugin.getServer().getScheduler().runTask(plugin, DeferredPlacement.after(event, () -> {
            if (families.get(parentId) != family
                    || !block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return false;
            var current = NexoBlocks.customBlockMechanic(block);
            return current != null && current.getItemID().equals(parentId);
        }, () -> block.setBlockData(data, false)));
    }

    private record Family(DirectionalType type, Map<String, String> directions) {}
    private record FamilyPlan(Family family, List<ConfigurationSection> children) {}
    private record Placement(org.bukkit.Location location, BlockFace face) {}
}
