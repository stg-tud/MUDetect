package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumNode;

import java.util.function.Function;

public class EquallyLabelledNodeMatcher implements NodeMatcher {
    private final Function<EGroumNode, String> getLabel;

    public EquallyLabelledNodeMatcher() {
        this(EGroumNode::getLabel);
    }

    public EquallyLabelledNodeMatcher(Function<EGroumNode, String> getLabel) {
        this.getLabel = getLabel;
    }

    @Override
    public boolean test(EGroumNode node1, EGroumNode node2) {
        return getLabel.apply(node1).equals(getLabel.apply(node2));
    }
}
