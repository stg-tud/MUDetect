package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.typehierarchy.TypeHierarchy;

public class SubtypeDataNodeMatcher implements NodeMatcher {

    private TypeHierarchy typeHierarchy;

    public SubtypeDataNodeMatcher(TypeHierarchy typeHierarchy) {
        this.typeHierarchy = typeHierarchy;
    }

    @Override
    public boolean test(Node targetNode, Node patternNode) {
        return typeHierarchy.isA(targetNode.getLabel(), patternNode.getLabel());
    }
}
