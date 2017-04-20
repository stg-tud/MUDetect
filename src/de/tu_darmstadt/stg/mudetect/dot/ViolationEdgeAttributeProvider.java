package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumEdge;

import java.util.Map;

class ViolationEdgeAttributeProvider extends AUGEdgeAttributeProvider {
    private final Overlap violation;
    private final String unmappedNodeColor;

    ViolationEdgeAttributeProvider(Overlap violation, String unmappedNodeColor) {
        this.unmappedNodeColor = unmappedNodeColor;
        this.violation = violation;
    }

    @Override
    public Map<String, String> getComponentAttributes(EGroumEdge edge) {
        final Map<String, String> attributes = super.getComponentAttributes(edge);
        if (!violation.mapsEdge(edge)) {
            attributes.put("color", unmappedNodeColor);
            attributes.put("fontcolor", unmappedNodeColor);
        }
        return attributes;
    }
}
