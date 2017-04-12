package de.tu_darmstadt.stg.mudetect.typehierarchy;

import java.util.*;

public class TypeHierarchy {
    private Map<String, Set<String>> supertypesByType = new HashMap<>();

    protected TypeHierarchy() {
        addSupertypes("char", Arrays.asList("byte", "short", "int", "long", "float", "double"));
        addSupertypes("byte", Arrays.asList("short", "int", "long", "float", "double"));
        addSupertypes("short", Arrays.asList("int", "long", "float", "double"));
        addSupertypes("int", Arrays.asList("long", "float", "double"));
        addSupertypes("long", Arrays.asList("float", "double"));
        addSupertype("float", "double");
    }

    public boolean isA(String type, String supertype) {
        Set<String> supertypes = getSupertypes(type);
        return type.equals(supertype) ||
                supertypes.contains(supertype);
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
