package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.typehierarchy.TypeHierarchy;
import egroum.EGroumNode;

public class SubtypeNodeMatcher implements NodeMatcher {

    private TypeHierarchy typeHierarchy;

    public SubtypeNodeMatcher(TypeHierarchy typeHierarchy) {
        this.typeHierarchy = typeHierarchy;
    }

    @Override
    public boolean test(EGroumNode targetNode, EGroumNode patternNode) {
        return typeHierarchy.isA(targetNode.getLabel(), patternNode.getLabel());
    }
}
