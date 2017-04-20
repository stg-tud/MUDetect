package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumNode;
import org.jgrapht.ext.VertexNameProvider;

class ViolationPatternNodeNameProvider implements VertexNameProvider<EGroumNode> {
    private static final ViolationTargetNodeNameProvider targetNodeNameProvider = new ViolationTargetNodeNameProvider();

    private final Overlap overlap;

    ViolationPatternNodeNameProvider(Overlap overlap) {
        this.overlap = overlap;
    }

    @Override
    public String getVertexName(EGroumNode patternNode) {
        StringBuilder label = new StringBuilder(patternNode.getLabel());
        if (overlap.mapsNode(patternNode)) {
            EGroumNode targetNode = overlap.getMappedTargetNode(patternNode);
            if (!patternNode.getLabel().equals(targetNode.getLabel())) {
                label.append("\\n(").append(targetNodeNameProvider.getVertexName(targetNode)).append(")");
            }
        }
        return label.toString();
    }
}
