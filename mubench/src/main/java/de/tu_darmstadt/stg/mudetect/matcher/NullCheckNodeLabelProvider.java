package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import utils.JavaASTUtil;

import java.util.Optional;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

public class NullCheckNodeLabelProvider implements NodeLabelProvider {
    @Override
    public Optional<String> apply(EGroumNode node) {
        if (node instanceof EGroumDataNode && node.getLabel().equals("null")) {
            if (node.getOutEdges().stream().anyMatch(this::isConditionParameter)) {
                return Optional.of("null");
            }
        }
        return Optional.empty();
    }

    private boolean isConditionParameter(EGroumEdge edge) {
        return edge.isParameter() && (targetIs(edge, EQUALS) || targetIs(edge, NOT_EQUALS));
    }

    private boolean targetIs(EGroumEdge edge, InfixExpression.Operator equals) {
        return edge.getTarget().getLabel().equals(JavaASTUtil.getLabel(equals));
    }
}
