package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import org.eclipse.jdt.core.dom.ASTNode;

public class CallNodeDoubleWeightFunction implements NodeWeightFunction {
    @Override
    public double getWeight(Node node) {
        return node instanceof MethodCallNode ? 2 : 1;
    }
}
