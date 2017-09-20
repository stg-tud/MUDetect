package de.tu_darmstadt.stg.mudetect.aug.model.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class AUGNodeAttributeProvider implements ComponentAttributeProvider<Node> {
    @Override
    public Map<String, String> getComponentAttributes(Node node) {
        final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (node instanceof ActionNode) {
            attributes.put("shape", "box");
        } else if (node instanceof DataNode) {
            attributes.put("shape", "ellipse");
        }
        return attributes;
    }
}
