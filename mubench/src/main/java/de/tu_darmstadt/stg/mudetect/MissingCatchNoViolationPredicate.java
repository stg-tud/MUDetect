package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.CatchNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ThrowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.data.ExceptionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.Set;

public class MissingCatchNoViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        return isMissingCatchOnly(overlap) || isMissingThrowOnly(overlap) ? Optional.of(false) : Optional.empty();
    }

    private boolean isMissingCatchOnly(Overlap overlap) {
        Set<Edge> missingEdges = overlap.getMissingEdges();
        Set<Node> missingNodes = overlap.getMissingNodes();

        if (missingEdges.size() != 2 || missingNodes.size() != 2) {
            return false;
        }

        Edge throwEdge = null;
        Edge paraEdge = null;
        for (Edge missingEdge : missingEdges) {
            if (missingEdge instanceof ThrowEdge) {
                throwEdge = missingEdge;
            } else if (missingEdge instanceof ParameterEdge) {
                paraEdge = missingEdge;
            }
        }

        if (throwEdge == null || paraEdge == null) {
            return false;
        }

        Node exceptionNode = null;
        Node catchNode = null;

        for (Node missingNode : missingNodes) {
            if (missingNode instanceof CatchNode) {
                catchNode = missingNode;
            } else if (missingNode instanceof DataNode) {
                // We're content with an object (i.e., data node) that is thrown, the compiler should ensure it is a Throwable.
                exceptionNode = missingNode;
            }
        }

        // -(throw)-> (Exception) -(para)-> <catch>
        return exceptionNode != null && catchNode != null
                && throwEdge.getTarget() == exceptionNode
                && exceptionNode == paraEdge.getSource() && paraEdge.getTarget() == catchNode;
    }

    private boolean isMissingThrowOnly(Overlap overlap) {
        Set<Edge> missingEdges = overlap.getMissingEdges();
        Set<Node> missingNodes = overlap.getMissingNodes();

        if (missingEdges.size() != 1 || missingNodes.size() != 1) {
            return false;
        }

        Node missingNode = missingNodes.iterator().next();
        Edge missingEdge = missingEdges.iterator().next();
        // We're content with any object (i.e., data node) that is thrown, the compiler should ensure it is a Throwable.
        return missingNode instanceof DataNode && missingEdge instanceof ThrowEdge
                && missingEdge.getTarget() == missingNode;
    }
}
