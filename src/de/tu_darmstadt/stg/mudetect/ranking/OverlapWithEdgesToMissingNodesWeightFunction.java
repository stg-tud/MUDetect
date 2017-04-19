package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.Set;

public class OverlapWithEdgesToMissingNodesWeightFunction implements ViolationWeightFunction {
    private NodeWeightFunction nodeWeight;

    public OverlapWithEdgesToMissingNodesWeightFunction(NodeWeightFunction nodeWeight) {
        this.nodeWeight = nodeWeight;
    }

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return 1 - (getMissingElementWeight(violation) / getPatternWeight(violation));
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        double missingElementsWeight = getMissingElementWeight(violation);
        double patternWeight = getPatternWeight(violation);
        return String.format("overlap = %.2f / %.2f", patternWeight - missingElementsWeight, patternWeight);
    }

    private double getMissingElementWeight(Overlap violation) {
        Set<EGroumNode> missingNodes = violation.getMissingNodes();
        Set<EGroumEdge> missingEdges = violation.getMissingEdges();
        return nodeWeight.getInverseWeight(missingNodes) + getNumberOfMissingEdgesBetweenMappedNodes(violation);
    }

    private int getNumberOfMissingEdgesBetweenMappedNodes(Overlap violation) {
        Set<EGroumNode> missingNodes = violation.getMissingNodes();
        Set<EGroumEdge> missingEdges = violation.getMissingEdges();
        return (int) missingEdges.stream()
                .filter(edge -> !missingNodes.contains(edge.getSource()) && !missingNodes.contains(edge.getTarget()))
                .count();
    }

    private double getPatternWeight(Overlap violation) {
        Pattern pattern = violation.getPattern();
        return nodeWeight.getInverseWeight(pattern.vertexSet()) + pattern.getEdgeSize();
    }
}
