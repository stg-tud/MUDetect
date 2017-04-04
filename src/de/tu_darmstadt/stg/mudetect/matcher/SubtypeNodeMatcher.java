package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.model.TypeHierarchy;
import egroum.EGroumNode;

public class SubtypeNodeMatcher implements NodeMatcher {

    private TypeHierarchy typeHierarchy;

    public SubtypeNodeMatcher(TypeHierarchy typeHierarchy) {
        this.typeHierarchy = typeHierarchy;
    }

    @Override
    public boolean test(EGroumNode node1, EGroumNode node2) {
        return typeHierarchy.isA(node1.getLabel(), node2.getLabel());
    }
}
