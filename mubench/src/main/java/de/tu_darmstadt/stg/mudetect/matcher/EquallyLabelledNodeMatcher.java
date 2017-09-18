package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.Node;
import egroum.EGroumNode;

import java.util.function.Function;

public class EquallyLabelledNodeMatcher implements NodeMatcher {
    private final Function<Node, String> getLabel;

    public EquallyLabelledNodeMatcher() {
        this(Node::getLabel);
    }

    public EquallyLabelledNodeMatcher(Function<Node, String> getLabel) {
        this.getLabel = getLabel;
    }

    @Override
    public boolean test(Node node1, Node node2) {
        return getLabel.apply(node1).equals(getLabel.apply(node2));
    }
}
