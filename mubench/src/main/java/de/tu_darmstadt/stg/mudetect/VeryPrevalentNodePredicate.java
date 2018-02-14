package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.HashSet;
import java.util.Set;

public class VeryPrevalentNodePredicate {
    private static final Set<String> veryPrevalentTypes = new HashSet<>();

    static {
        veryPrevalentTypes.add("Object");
        veryPrevalentTypes.add("String");
        veryPrevalentTypes.add("Arrays");
        veryPrevalentTypes.add("Throwable");
        veryPrevalentTypes.add("Exception");
    }

    public static boolean isVeryPrevalent(Node node) {
        if (node instanceof MethodCallNode) {
            String declaringTypeName = ((MethodCallNode) node).getDeclaringTypeName();
            return veryPrevalentTypes.contains(declaringTypeName);
        }
        return false;
    }
}
