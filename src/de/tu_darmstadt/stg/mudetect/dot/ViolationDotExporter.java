package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViolationDotExporter {
    /**
     * Returns a dot-graph representation of the pattern with all the violating elements marked.
     */
    public String toDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        return new AUGDotExporter(
                new ViolationNodeAttributeProvider(overlap, "red"),
                new ViolationEdgeAttributeProvider(overlap, "red"))
                .toDotGraph(overlap.getPattern());
    }

    /**
     * Returns a dot-graph representation of the target with all the pattern elements marked.
     */
    public String toTargetDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        AUG target = overlap.getTarget();
        return toTargetDotGraph(overlap, target, new HashMap<>());
    }

    private String toTargetDotGraph(Overlap instance, AUG target, Map<String, String> graphAttributes) {
        return new AUGDotExporter(
                new ViolationNodeAttributeProvider(instance, "gray"),
                new ViolationEdgeAttributeProvider(instance, "gray"))
                .toDotGraph(target, graphAttributes);
    }

    /**
     * Returns a dot-graph representation of a fragment of the target with all the pattern elements marked. The fragment
     * includes all pattern nodes, all target nodes with a direct incoming or outgoing edge from a pattern node, and
     * all respective edges.
     */
    public String toTargetEnvironmentDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        AUG targetEnvironment = getTargetEnvironmentAUG(overlap);
        return toTargetDotGraph(overlap, targetEnvironment, new HashMap<String, String>() {{ put("nslimit", "10000"); }});
    }

    private static AUG getTargetEnvironmentAUG(Overlap overlap) {
        AUG target = overlap.getTarget();
        AUG envAUG = new AUG(target.getLocation().getMethodName(), target.getLocation().getFilePath());
        for (EGroumNode mappedTargetNode : overlap.getMappedTargetNodes()) {
            envAUG.addVertex(mappedTargetNode);
            for (EGroumEdge edge : target.edgesOf(mappedTargetNode)) {
                envAUG.addVertex(edge.getSource());
                envAUG.addVertex(edge.getTarget());
            }
        }
        Set<EGroumNode> envNodes = envAUG.vertexSet();
        for (EGroumNode node : envNodes) {
            for (EGroumEdge edge : target.edgesOf(node)) {
                if (envNodes.contains(edge.getSource()) && envNodes.contains(edge.getTarget())) {
                    envAUG.addEdge(edge.getSource(), edge.getTarget(), edge);
                }
            }
        }
        return envAUG;
    }
}
