package edu.iastate.cs.egroum.aug;

import java.util.function.Predicate;

public class ContainsTypeUsagePredicate implements Predicate<EGroumGraph> {
    private String typeName;

    public ContainsTypeUsagePredicate(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public boolean test(EGroumGraph graph) {
        return graph.getNodes().stream().anyMatch(node -> node.getLabel().contains(typeName));
    }
}
