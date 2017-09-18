package de.tu_darmstadt.stg.mudetect.aug.dot;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import org.jgrapht.ext.VertexNameProvider;

import java.util.Optional;

public class AUGNodeNameProvider implements VertexNameProvider<Node> {
    @Override
    public String getVertexName(Node targetNode) {
        StringBuilder label = new StringBuilder(targetNode.getLabel());
        if (targetNode instanceof ActionNode) {
            APIUsageGraph graph = targetNode.getGraph();
            if (graph instanceof APIUsageExample) {
                Optional<Integer> sourceLineNumber = ((APIUsageExample) graph).getSourceLineNumber(targetNode);
                sourceLineNumber.ifPresent(integer -> label.append(" L").append(integer));
            }
        }
        return label.toString();
    }
}
