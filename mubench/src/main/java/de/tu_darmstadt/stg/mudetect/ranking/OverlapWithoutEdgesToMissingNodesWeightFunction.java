package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

import java.util.Set;

public class OverlapWithoutEdgesToMissingNodesWeightFunction implements ViolationWeightFunction {
    private NodeWeightFunction nodeWeight;

    public OverlapWithoutEdgesToMissingNodesWeightFunction(NodeWeightFunction nodeWeight) {
        this.nodeWeight = nodeWeight;
    }

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return getMappedElementsWeight(violation) / getPatternWeight(violation);
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        double mappedElementsWeight = getMappedElementsWeight(violation);
        double patternWeight = getPatternWeight(violation);
        return String.format("overlap = %.2f / %.2f", mappedElementsWeight, patternWeight);
    }

    private double getMappedElementsWeight(Overlap violation) {
        Set<Node> mappedNodes = violation.getMappedTargetNodes();
        Set<Edge> mappedEdges = violation.getMappedTargetEdges();
        return nodeWeight.getInverseWeight(mappedNodes) + mappedEdges.size();
    }

    private double getPatternWeight(Overlap violation) {
        APIUsagePattern pattern = violation.getPattern();
        return nodeWeight.getInverseWeight(pattern.vertexSet()) + getNumberOfEdgesBetweenMappedNodes(violation, pattern);
    }

    private long getNumberOfEdgesBetweenMappedNodes(Overlap violation, APIUsagePattern pattern) {
        Set<Node> missingNodes = violation.getMissingNodes();
        return pattern.edgeSet().stream().filter(patternEdge -> isConnectedTo(patternEdge, missingNodes)).count();
    }

    private boolean isConnectedTo(Edge patternEdge, Set<Node> missingNodes) {
        return !missingNodes.contains(patternEdge.getSource()) && !missingNodes.contains(patternEdge.getTarget());
    }

    @Override
    public String getId() {
        return "O-E2MN";
    }
}
