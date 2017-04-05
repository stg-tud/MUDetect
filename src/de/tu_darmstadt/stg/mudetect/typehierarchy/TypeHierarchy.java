package de.tu_darmstadt.stg.mudetect.typehierarchy;

import java.util.*;

public class TypeHierarchy {
    private Map<String, Set<String>> supertypesByType = new HashMap<>();

    protected TypeHierarchy() {
        addSupertype("byte", "short");
        addSupertype("short", "int");
        addSupertype("int", "long");
        addSupertype("long", "float");
        addSupertype("float", "double");
        addSupertype("String", "CharSequence");
    }

    public boolean isA(String type, String supertype) {
        Set<String> supertypes = getSupertypes(type);
        return type.equals(supertype) ||
                supertypes.contains(supertype) ||
                supertypes.stream().anyMatch(intermediate -> isA(intermediate, supertype));
    }

    private Set<String> getSupertypes(String type) {
        if (supertypesByType.containsKey(type)) {
            return supertypesByType.get(type);
        } else {
            return new HashSet<>();
        }
    }

    protected void addSupertype(String type, String supertype) {
        if (!supertypesByType.containsKey(type)) {
            supertypesByType.put(type, new HashSet<>());
        }
        if (!type.equals(supertype)) {
            supertypesByType.get(type).add(supertype);
        }
    }

    void addSupertypes(String type, Iterable<String> supertypes) {
        for (String supertype : supertypes) {
            addSupertype(type, supertype);
        }
    }
}
