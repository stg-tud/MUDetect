package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumNode;
import org.jgrapht.ext.VertexNameProvider;

class OverlapPatternAndTargetNodeNameProvider implements VertexNameProvider<EGroumNode> {
    private static final AUGNodeNameProvider TARGET_AUG_NODE_NAME_PROVIDER = new AUGNodeNameProvider();

    private final Overlap overlap;

    OverlapPatternAndTargetNodeNameProvider(Overlap overlap) {
        this.overlap = overlap;
    }

    @Override
    public String getVertexName(EGroumNode patternNode) {
        StringBuilder label = new StringBuilder(patternNode.getLabel());
        if (overlap.mapsNode(patternNode)) {
            EGroumNode targetNode = overlap.getMappedTargetNode(patternNode);
            if (!patternNode.getLabel().equals(targetNode.getLabel())) {
                label.append("\\n(").append(TARGET_AUG_NODE_NAME_PROVIDER.getVertexName(targetNode)).append(")");
            }
        }
        return label.toString();
    }
}
