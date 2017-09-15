package de.tu_darmstadt.stg.mudetect.ranking;

import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.ASTNode;

public class CallNodeDoubleWeightFunction implements NodeWeightFunction {
    @Override
    public double getWeight(EGroumNode node) {
        return node.getAstNodeType() == ASTNode.METHOD_INVOCATION ? 2 : 1;
    }
}
