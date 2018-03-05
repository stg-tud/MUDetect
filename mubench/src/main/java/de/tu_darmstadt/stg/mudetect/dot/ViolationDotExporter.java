package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.WithSourceLineNumberLabelProvider;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Violation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViolationDotExporter {
    /**
     * Returns a dot-graph representation of the pattern with all the violating elements marked.
     */
    public String toDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        BaseAUGLabelProvider baseLabelProvider = new BaseAUGLabelProvider();
        AUGLabelProvider withLineLabelProvider = new WithSourceLineNumberLabelProvider(baseLabelProvider);
        return new AUGDotExporter(
                node -> overlap.mapsNode(node) ? overlap.getMappedTargetNode(node).apply(withLineLabelProvider) : node.apply(baseLabelProvider),
                edge -> edge.apply(withLineLabelProvider),
                new OverlapNodeAttributeProvider(overlap, "red"),
                new OverlapEdgeAttributeProvider(overlap, "red"))
                .toDotGraph(overlap.getPattern());
    }

    /**
     * Returns a dot-graph representation of the target with all the pattern elements marked.
     */
    public String toTargetDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        APIUsageExample target = overlap.getTarget();
        return toTargetDotGraph(overlap, target, new HashMap<>());
    }

    private String toTargetDotGraph(Overlap instance, APIUsageExample target, Map<String, String> graphAttributes) {
        AUGLabelProvider labelProvider = new WithSourceLineNumberLabelProvider(new BaseAUGLabelProvider());
        return new AUGDotExporter(
                labelProvider::getLabel, labelProvider::getLabel,
                new OverlapNodeAttributeProvider(instance, "gray"),
                new OverlapEdgeAttributeProvider(instance, "gray"),
                graph -> graphAttributes)
                .toDotGraph(target);
    }

    /**
     * Returns a dot-graph representation of a fragment of the target with all the pattern elements marked. The fragment
     * includes all pattern nodes, all target nodes with a direct incoming or outgoing edge from a pattern node, and
     * all respective edges.
     */
    public String toTargetEnvironmentDotGraph(Violation violation) {
        Overlap overlap = violation.getOverlap();
        APIUsageExample targetEnvironment = getTargetEnvironmentAUG(overlap);
        return toTargetDotGraph(overlap, targetEnvironment, new HashMap<String, String>() {{ put("nslimit", "10000"); }});
    }

    private static APIUsageExample getTargetEnvironmentAUG(Overlap overlap) {
        APIUsageExample target = overlap.getTarget();
        APIUsageExample envAUG = new APIUsageExample(target.getLocation());
        for (Node mappedTargetNode : overlap.getMappedTargetNodes()) {
            envAUG.addVertex(mappedTargetNode);
            for (Edge edge : target.edgesOf(mappedTargetNode)) {
                // exclude nodes that are only connected via order edges
                if (!(edge instanceof OrderEdge)) {
                    envAUG.addVertex(target.getEdgeSource(edge));
                    envAUG.addVertex(target.getEdgeTarget(edge));
                }
            }
        }
        Set<Node> envNodes = envAUG.vertexSet();
        for (Node node : envNodes) {
            for (Edge edge : target.edgesOf(node)) {
                Node edgeSource = target.getEdgeSource(edge);
                Node edgeTarget = target.getEdgeTarget(edge);
                if (envNodes.contains(edgeSource) && envNodes.contains(edgeTarget)) {
                    envAUG.addEdge(edgeSource, edgeTarget, edge);
                }
            }
        }
        return envAUG;
    }
}
