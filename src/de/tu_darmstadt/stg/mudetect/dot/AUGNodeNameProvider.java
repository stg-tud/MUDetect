package de.tu_darmstadt.stg.mudetect.dot;

import egroum.EGroumActionNode;
import egroum.EGroumNode;
import org.jgrapht.ext.VertexNameProvider;

import java.util.Optional;

class AUGNodeNameProvider implements VertexNameProvider<EGroumNode> {
    @Override
    public String getVertexName(EGroumNode targetNode) {
        StringBuilder label = new StringBuilder(targetNode.getLabel());
        if (targetNode instanceof EGroumActionNode) {
            Optional<Integer> sourceLineNumber = targetNode.getSourceLineNumber();
            if (sourceLineNumber.isPresent()) {
                label.append(" L").append(sourceLineNumber);
            }
        }
        return label.toString();
    }
}
