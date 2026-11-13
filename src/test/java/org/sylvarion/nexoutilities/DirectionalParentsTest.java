package org.sylvarion.nexoutilities;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DirectionalParentsTest {
    @Test
    void resolvesExistingAndGeneratedVariantsToBaseItem() {
        var parents = new DirectionalParents(Map.of("lamp-north", List.of(
                "lamp-north", "lamp-east", "lamp-south", "lamp-west", "generated-child")));
        for (String variant : List.of("lamp-north", "lamp-east", "lamp-south", "lamp-west", "generated-child")) {
            assertEquals("lamp-north", parents.parent(variant));
        }
        assertNull(parents.parent("unrelated"));
    }

    @Test
    void repeatedDoubleArmReferencesHaveOneParent() {
        var parents = new DirectionalParents(Map.of("arm-ns", List.of("arm-ns", "arm-ns", "arm-we", "arm-we")));
        assertEquals("arm-ns", parents.parent("arm-we"));
    }

    @Test
    void leavesAmbiguousVariantsAloneAndKeepsIndependentBases() {
        var parents = new DirectionalParents(Map.of(
                "first", List.of("shared", "second"), "second", List.of("shared")));
        assertNull(parents.parent("shared"));
        assertEquals("second", parents.parent("second"));
        assertNull(new DirectionalParents(Map.of()).parent("first"));
    }
}
