package org.sylvarion.nexoutilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;

final class DirectionalConfigs {
    static final String BLOCK = "Mechanics.custom_block";
    static final String PARENT = "nexoutilities_parent";

    private DirectionalConfigs() {}

    static ConfigurationSection child(String id, String parent, ConfigurationSection source,
                                      int variation, String model, DirectionalType.Rotation rotation) {
        ConfigurationSection child = new YamlConfiguration().createSection(id);
        child.set("material", "PAPER");
        child.set(PARENT, parent);
        copy(source, child.createSection(BLOCK));
        child.set(BLOCK + ".directional", null);
        child.set(BLOCK + ".custom_variation", variation);
        child.set(BLOCK + ".model", model);
        child.set(BLOCK + ".variants", List.of(Map.of("model", model, "x", rotation.x(), "y", rotation.y())));
        return child;
    }

    static void copy(ConfigurationSection source, ConfigurationSection destination) {
        source.getValues(false).forEach((key, value) -> {
            if (value instanceof ConfigurationSection section) copy(section, destination.createSection(key));
            else destination.set(key, value);
        });
    }
}
