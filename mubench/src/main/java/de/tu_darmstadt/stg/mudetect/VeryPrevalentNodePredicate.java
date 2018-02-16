package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class VeryPrevalentNodePredicate implements Predicate<Node> {
    private static final Set<String> veryPrevalentTypes = new HashSet<>();

    static {
        veryPrevalentTypes.add("Object");
        veryPrevalentTypes.add("Class");
        veryPrevalentTypes.add("String");
        veryPrevalentTypes.add("Arrays");
        veryPrevalentTypes.add("System");
        veryPrevalentTypes.add("Throwable");
        veryPrevalentTypes.add("Exception");
    }

    @Override
    public boolean test(Node node) {
        if (node instanceof MethodCallNode) {
            String declaringTypeName = ((MethodCallNode) node).getDeclaringTypeName();
            return veryPrevalentTypes.contains(declaringTypeName);
        }
        return false;
    }
}
