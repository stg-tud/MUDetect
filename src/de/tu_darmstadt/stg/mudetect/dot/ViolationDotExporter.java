package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Violation;

public class ViolationDotExporter {
    /**
     * Returns a dot-graph representation of the pattern with all the violating elements marked.
     */
    public String toDotGraph(Violation violation) {
        Instance instance = violation.getInstance();
        return new AUGDotExporter(
                new ViolationNodeAttributeProvider(instance, "red"),
                new ViolationEdgeAttributeProvider(instance, "red"))
                .toDotGraph(instance.getPattern());
    }

    /**
     * Returns a dot-graph representation of the target with all the pattern elements marked.
     */
    public String toTargetDotGraph(Violation violation) {
        Instance instance = violation.getInstance();
        return new AUGDotExporter(
                new ViolationNodeAttributeProvider(instance, "gray"),
                new ViolationEdgeAttributeProvider(instance, "gray"))
                .toDotGraph(instance.getTarget());
    }
}
