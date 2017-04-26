package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Set;
import java.util.stream.Collectors;

public class MinCallSizeModel implements Model {
    private Model model;
    private final int minNumberOfCalls;

    public MinCallSizeModel(Model model, int minNumberOfCalls) {
        this.model = model;
        this.minNumberOfCalls = minNumberOfCalls;
    }

    @Override
    public Set<Pattern> getPatterns() {
        return model.getPatterns().stream().filter(this::hasEnoughCalls).collect(Collectors.toSet());
    }

    private boolean hasEnoughCalls(Pattern pattern) {
        return pattern.vertexSet().stream().filter(this::isMethodCall).count() >= minNumberOfCalls;
    }

    private boolean isMethodCall(EGroumNode node) {
        int nodeType = node.getAstNodeType();
        return nodeType == ASTNode.METHOD_INVOCATION || nodeType == ASTNode.CLASS_INSTANCE_CREATION;
    }
}
