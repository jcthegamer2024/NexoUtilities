package org.sylvarion.nexoutilities;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@NullMarked
final class DirectionalParents {
    private final Map<String, String> parents;

    DirectionalParents(Map<String, ? extends Collection<String>> families) {
        Map<String, String> owners = new HashMap<>();
        var ambiguous = new HashSet<String>();
        families.forEach((parent, variants) -> {
            owners.put(parent, parent);
            for (String variant : variants) {
                // A separately configured base remains its own pick-block item.
                if (families.containsKey(variant)) continue;
                String previous = owners.putIfAbsent(variant, parent);
                if (previous != null && !previous.equals(parent)) ambiguous.add(variant);
            }
        });
        ambiguous.forEach(owners::remove);
        parents = Map.copyOf(owners);
    }

    @Nullable String parent(String variant) {
        return parents.get(variant);
    }
}
