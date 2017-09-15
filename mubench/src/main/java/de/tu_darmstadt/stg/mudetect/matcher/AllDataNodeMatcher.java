package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumDataNode;
import egroum.EGroumNode;

public class AllDataNodeMatcher implements NodeMatcher {
    @Override
    public boolean test(EGroumNode targetNode, EGroumNode patternNode) {
        return targetNode instanceof EGroumDataNode && patternNode instanceof EGroumDataNode;
    }
}
