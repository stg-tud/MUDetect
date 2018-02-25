package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class VeryUnspecificReceiverTypePredicate implements Predicate<Node> {
    private static final Set<String> veryUnspecificTypes = new HashSet<>();

    static {
        veryUnspecificTypes.add("Object");
        veryUnspecificTypes.add("Class");
        veryUnspecificTypes.add("System");
        veryUnspecificTypes.add("AutoCloseable");
        veryUnspecificTypes.add("Throwable");
        veryUnspecificTypes.add("Exception");
        veryUnspecificTypes.add("String");
        veryUnspecificTypes.add("CharSequence");
        veryUnspecificTypes.add("StringBuilder");
        veryUnspecificTypes.add("AbstractStringBuilder");
        veryUnspecificTypes.add("StringBuffer");
        veryUnspecificTypes.add("Appendable");
        veryUnspecificTypes.add("Arrays");
    }

    @Override
    public boolean test(Node node) {
        if (node instanceof MethodCallNode) {
            String declaringTypeName = ((MethodCallNode) node).getDeclaringTypeName();
            return veryUnspecificTypes.contains(declaringTypeName);
        }
        return false;
    }
}
