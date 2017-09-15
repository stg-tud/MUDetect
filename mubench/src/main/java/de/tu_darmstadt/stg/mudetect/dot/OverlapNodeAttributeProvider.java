package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumNode;

import java.util.Map;

class OverlapNodeAttributeProvider extends AUGNodeAttributeProvider {
    private final Overlap violation;
    private final String unmappedNodeColor;

    OverlapNodeAttributeProvider(Overlap violation, String unmappedNodeColor) {
        this.violation = violation;
        this.unmappedNodeColor = unmappedNodeColor;
    }

    @Override
    public Map<String, String> getComponentAttributes(EGroumNode node) {
        final Map<String, String> attributes = super.getComponentAttributes(node);
        if (!violation.mapsNode(node)) {
            attributes.put("color", unmappedNodeColor);
            attributes.put("fontcolor", unmappedNodeColor);
        }
        return attributes;
    }
}
