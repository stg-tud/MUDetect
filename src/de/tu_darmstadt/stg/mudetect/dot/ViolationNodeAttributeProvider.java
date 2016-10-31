package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import egroum.EGroumNode;

import java.util.Map;

public class ViolationNodeAttributeProvider extends AUGNodeAttributeProvider {
    private final Instance instance;
    private final String unmappedNodeColor;

    public ViolationNodeAttributeProvider(Instance instance, String unmappedNodeColor) {
        this.instance = instance;
        this.unmappedNodeColor = unmappedNodeColor;
    }

    @Override
    public Map<String, String> getComponentAttributes(EGroumNode node) {
        final Map<String, String> attributes = super.getComponentAttributes(node);
        if (!instance.mapsNode(node)) {
            attributes.put("color", unmappedNodeColor);
            attributes.put("fontcolor", unmappedNodeColor);
        }
        return attributes;
    }
}
