package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import egroum.EGroumEdge;

import java.util.Map;

public class ViolationEdgeAttributeProvider extends AUGEdgeAttributeProvider {
    private final Instance instance;
    private final String unmappedNodeColor;

    public ViolationEdgeAttributeProvider(Instance instance, String unmappedNodeColor) {
        this.unmappedNodeColor = unmappedNodeColor;
        this.instance = instance;
    }

    @Override
    public Map<String, String> getComponentAttributes(EGroumEdge edge) {
        final Map<String, String> attributes = super.getComponentAttributes(edge);
        if (!instance.mapsPatternEdge(edge)) {
            attributes.put("color", unmappedNodeColor);
            attributes.put("fontcolor", unmappedNodeColor);
        }
        return attributes;
    }
}
