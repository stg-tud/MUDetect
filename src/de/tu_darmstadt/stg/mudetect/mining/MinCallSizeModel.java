package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Set;
import java.util.stream.Collectors;

public class MinCallSizeModel implements Model {

    private final Set<Pattern> patterns;

    public MinCallSizeModel(Model model, int minNumberOfCalls) {
        patterns = model.getPatterns().stream()
                .filter((pattern) -> hasEnoughCalls(pattern, minNumberOfCalls))
                .collect(Collectors.toSet());
    }

    private boolean hasEnoughCalls(Pattern pattern, int minNumberOfCalls) {
        long numberOfCalls = pattern.vertexSet().stream().filter(MinCallSizeModel::isMethodCall).count();
        long numberOfThrows = pattern.edgeSet().stream().filter(EGroumEdge::isThrow).count();
        return numberOfCalls + numberOfThrows >= minNumberOfCalls;
    }

    private static boolean isMethodCall(EGroumNode node) {
        int nodeType = node.getAstNodeType();
        return nodeType == ASTNode.METHOD_INVOCATION || nodeType == ASTNode.CLASS_INSTANCE_CREATION;
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
