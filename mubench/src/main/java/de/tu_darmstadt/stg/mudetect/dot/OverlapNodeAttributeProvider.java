package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGNodeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Map;

class OverlapNodeAttributeProvider extends AUGNodeAttributeProvider {
    private final Overlap violation;
    private final String unmappedNodeColor;

    OverlapNodeAttributeProvider(Overlap violation, String unmappedNodeColor) {
        this.violation = violation;
        this.unmappedNodeColor = unmappedNodeColor;
    }

    @Override
    public Map<String, String> getComponentAttributes(Node node) {
        final Map<String, String> attributes = super.getComponentAttributes(node);
        if (!violation.mapsNode(node)) {
            attributes.put("color", unmappedNodeColor);
            attributes.put("fontcolor", unmappedNodeColor);
        }
        return attributes;
    }
}
