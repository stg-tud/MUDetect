package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumNode;

import java.util.function.BiPredicate;

public interface NodeMatcher extends BiPredicate<EGroumNode, EGroumNode> {
    /**
     * @param targetNode the target node to match
     * @param patternNode the pattern node to match against
     * @return whether the target node matches the pattern node (need not be symmetric!)
     */
    @Override
    boolean test(EGroumNode targetNode, EGroumNode patternNode);
}
