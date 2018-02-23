package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.ConstructorCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.function.Predicate;

public class InstanceMethodCallPredicate implements Predicate<Node> {
    @Override
    public boolean test(Node node) {
        return node instanceof MethodCallNode && !(node instanceof ConstructorCallNode);
    }
}
