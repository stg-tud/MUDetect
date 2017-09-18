package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.Edge;
import de.tu_darmstadt.stg.mudetect.aug.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Set;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.SYNCHRONIZE;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.THROW;

public class MinPatternActionsModel implements Model {

    private final Set<APIUsagePattern> patterns;

    public MinPatternActionsModel(Model model, int minNumberOfCalls) {
        patterns = model.getPatterns().stream()
                .filter((pattern) -> hasEnoughCalls(pattern, minNumberOfCalls))
                .collect(Collectors.toSet());
    }

    private boolean hasEnoughCalls(APIUsagePattern pattern, int minNumberOfCalls) {
        long numberOfCalls = pattern.vertexSet().stream().filter(MinPatternActionsModel::isMethodCall).count();
        long numberOfThrows = pattern.edgeSet().stream().filter(this::isRelevant).count();
        return numberOfCalls + numberOfThrows >= minNumberOfCalls;
    }

    private boolean isRelevant(Edge edge) {
        return edge.getType() == THROW || edge.getType() == SYNCHRONIZE;
    }

    private static boolean isMethodCall(Node node) {
        return node instanceof MethodCallNode;
    }

    @Override
    public Set<APIUsagePattern> getPatterns() {
        return patterns;
    }
}
