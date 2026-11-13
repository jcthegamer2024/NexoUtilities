package org.sylvarion.nexoutilities;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionalConfigsTest {
    @Test
    void generatesNativeVariantsWithoutModifyingSource() throws Exception {
        var source = new YamlConfiguration();
        source.loadFromString("""
                type: CHORUSBLOCK
                custom_variation: 1
                directional:
                  type: LOG
                hardness: 3
                drop:
                  silktouch: true
                """);
        var child = DirectionalConfigs.child("child", "parent", source, 2,
                "example:block/log", DirectionalType.LOG.rotation("x", false));
        assertNull(child.get(DirectionalConfigs.BLOCK + ".directional"));
        assertEquals(2, child.getInt(DirectionalConfigs.BLOCK + ".custom_variation"));
        assertEquals(3, child.getInt(DirectionalConfigs.BLOCK + ".hardness"));
        assertTrue(child.getBoolean(DirectionalConfigs.BLOCK + ".drop.silktouch"));
        assertEquals(90, child.getMapList(DirectionalConfigs.BLOCK + ".variants").getFirst().get("x"));
        assertEquals("parent", child.getString(DirectionalConfigs.PARENT));
        assertEquals("LOG", source.getString("directional.type"));
        assertEquals(1, source.getInt("custom_variation"));
    }

    @Test
    void yamlRoundTripPreservesStateIdentityAndExplicitModel() throws Exception {
        var source = new YamlConfiguration();
        source.set("type", "STRINGBLOCK");
        var child = DirectionalConfigs.child("child", "parent", source, 127,
                "example:block/up", DirectionalType.DROPPER.rotation("up", true));
        var saved = new YamlConfiguration();
        DirectionalConfigs.copy(child, saved.createSection("child"));
        var loaded = new YamlConfiguration();
        loaded.loadFromString(saved.saveToString());
        assertEquals(127, loaded.getInt("child." + DirectionalConfigs.BLOCK + ".custom_variation"));
        assertEquals("example:block/up", loaded.getString("child." + DirectionalConfigs.BLOCK + ".model"));
        assertEquals(0, loaded.getMapList("child." + DirectionalConfigs.BLOCK + ".variants").getFirst().get("x"));
        assertEquals("parent", loaded.getString("child." + DirectionalConfigs.PARENT));
    }
}
