package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.Set;

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
        AUG target = instance.getTarget();
        return toTargetDotGraph(instance, target);
    }

    private String toTargetDotGraph(Instance instance, AUG target) {
        return new AUGDotExporter(
                new ViolationNodeAttributeProvider(instance, "gray"),
                new ViolationEdgeAttributeProvider(instance, "gray"))
                .toDotGraph(target);
    }

    /**
     * Returns a dot-graph representation of a fragment of the target with all the pattern elements marked. The fragment
     * includes all pattern nodes, all target nodes with a direct incoming or outgoing edge from a pattern node, and
     * all respective edges.
     */
    public String toTargetEnvironmentDotGraph(Violation violation) {
        Instance instance = violation.getInstance();
        AUG targetEnvironment = getTargetEnvironmentAUG(instance);
        return toTargetDotGraph(instance, targetEnvironment);
    }

    private static AUG getTargetEnvironmentAUG(Instance instance) {
        AUG target = instance.getTarget();
        AUG envAUG = new AUG(target.getLocation().getMethodName(), target.getLocation().getFilePath());
        Set<EGroumNode> mappedTargetNodes = instance.getMappedTargetNodes();
        for (EGroumNode mappedTargetNode : mappedTargetNodes) {
            envAUG.addVertex(mappedTargetNode);
            for (EGroumEdge edge : target.edgesOf(mappedTargetNode)) {
                envAUG.addVertex(edge.getSource());
                envAUG.addVertex(edge.getTarget());
                envAUG.addEdge(edge.getSource(), edge.getTarget(), edge);
            }
        }
        return envAUG;
    }
}
