package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.Set;

public class OverlapWithEdgesToMissingNodesWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
        Pattern pattern = violation.getPattern();
        return 1 - (getNumberOfMissingElementsWithoutEdgesToMissingNodes(violation) / (float) pattern.getSize());
    }

    @Override
    public String toString(Overlap violation, Overlaps overlaps, Model model) {
        Pattern pattern = violation.getPattern();
        return String.format("overlap = 1 - (%d / %d)", getNumberOfMissingElementsWithoutEdgesToMissingNodes(violation), pattern.getSize());
    }

    private int getNumberOfMissingElementsWithoutEdgesToMissingNodes(Overlap violation) {
        Set<EGroumNode> missingNodes = violation.getMissingNodes();
        Set<EGroumEdge> missingEdges = violation.getMissingEdges();
        int numberOfMissingElementsWithoutEdgesToMissingNodes = missingNodes.size();
        for (EGroumEdge missingEdge : missingEdges) {
            if (!missingNodes.contains(missingEdge.getSource()) && !missingNodes.contains(missingEdge.getTarget())) {
                numberOfMissingElementsWithoutEdgesToMissingNodes++;
            }
        }
        return numberOfMissingElementsWithoutEdgesToMissingNodes;
    }
}
