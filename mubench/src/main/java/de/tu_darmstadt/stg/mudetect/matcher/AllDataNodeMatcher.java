package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public class AllDataNodeMatcher implements NodeMatcher {
    @Override
    public boolean test(Node targetNode, Node patternNode) {
        return targetNode instanceof DataNode && patternNode instanceof DataNode;
    }
}
