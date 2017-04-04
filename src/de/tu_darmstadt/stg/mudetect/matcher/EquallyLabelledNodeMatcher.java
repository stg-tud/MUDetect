package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumNode;

public class EquallyLabelledNodeMatcher implements NodeMatcher {
    @Override
    public boolean test(EGroumNode node1, EGroumNode node2) {
        return node1.getLabel().equals(node2.getLabel());
    }
}
