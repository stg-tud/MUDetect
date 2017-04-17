package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumActionNode;
import egroum.EGroumNode;

/**
 * When a pattern contains a return (action) node, we interpret this to say that the returned value, i.e., the data node
 * that is a parameter to the return node, should be used <i>somehow</i> (as opposed to dropped). To reflect this idea
 * in the detection, we match any action node taking the respective parameter to the return node in the pattern.
 */
public class AnyToReturnMatcher implements NodeMatcher {
    @Override
    public boolean test(EGroumNode targetNode, EGroumNode patternNode) {
        return targetNode instanceof EGroumActionNode && patternNode.getLabel().equals("return");
    }
}
