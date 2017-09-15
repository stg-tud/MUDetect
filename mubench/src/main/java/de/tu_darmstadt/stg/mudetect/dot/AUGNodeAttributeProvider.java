package de.tu_darmstadt.stg.mudetect.dot;

import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumNode;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class AUGNodeAttributeProvider implements ComponentAttributeProvider<EGroumNode> {
    @Override
    public Map<String, String> getComponentAttributes(EGroumNode node) {
        final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (node instanceof EGroumActionNode) {
            attributes.put("shape", "box");
        } else if (node instanceof EGroumDataNode) {
            attributes.put("shape", "ellipse");
        }
        return attributes;
    }
}
