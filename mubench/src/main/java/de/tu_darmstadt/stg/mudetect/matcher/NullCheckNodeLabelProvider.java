package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import org.eclipse.jdt.core.dom.InfixExpression;
import edu.iastate.cs.egroum.utils.JavaASTUtil;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

public class NullCheckNodeLabelProvider implements NodeLabelProvider {
    @Override
    public Optional<String> apply(Node node) {
        if (node instanceof DataNode && node.getLabel().equals("null")) {
            if (node.getGraph().outgoingEdgesOf(node).stream().anyMatch(this::isConditionParameter)) {
                return Optional.of("null");
            }
        }
        return Optional.empty();
    }

    private boolean isConditionParameter(Edge edge) {
        return edge.getType() == PARAMETER && (targetIs(edge, EQUALS) || targetIs(edge, NOT_EQUALS));
    }

    private boolean targetIs(Edge edge, InfixExpression.Operator equals) {
        return edge.getTarget().getLabel().equals(JavaASTUtil.getLabel(equals));
    }
}
