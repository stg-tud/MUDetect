package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumNode;
import org.jgrapht.ext.VertexNameProvider;

class OverlapTargetNodeNameProvider implements VertexNameProvider<EGroumNode> {
    private static final AUGNodeNameProvider TARGET_AUG_NODE_NAME_PROVIDER = new AUGNodeNameProvider();

    private final Overlap overlap;

    OverlapTargetNodeNameProvider(Overlap overlap) {
        this.overlap = overlap;
    }

    @Override
    public String getVertexName(EGroumNode patternNode) {
        StringBuilder label = new StringBuilder();
        if (overlap.mapsNode(patternNode)) {
            EGroumNode targetNode = overlap.getMappedTargetNode(patternNode);
            label.append(TARGET_AUG_NODE_NAME_PROVIDER.getVertexName(targetNode));
        } else {
            label.append(patternNode.getLabel());
        }
        return label.toString();
    }
}
