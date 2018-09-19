package de.tu_darmstadt.stg.mudetect.aug.model.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.DefaultAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

public class AUGNodeAttributeProvider implements ComponentAttributeProvider<Node> {
    @Override
    public Map<String, Attribute> getComponentAttributes(Node node) {
        final LinkedHashMap<String, Attribute> attributes = new LinkedHashMap<>();
        if (node instanceof ActionNode) {
            attributes.put("shape", new DefaultAttribute<>("box", AttributeType.STRING));
        } else if (node instanceof DataNode) {
            attributes.put("shape", new DefaultAttribute<>("ellipse", AttributeType.STRING));
        }
        return attributes;
    }
}
