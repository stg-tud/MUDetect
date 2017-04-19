package de.tu_darmstadt.stg.mudetect.dot;

import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class AUGEdgeAttributeProvider implements ComponentAttributeProvider<EGroumEdge> {
    @Override
    public Map<String, String> getComponentAttributes(EGroumEdge edge) {
        final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        String style;
        if (edge instanceof EGroumDataEdge) {
            style = edge.isDirect() ? "solid" : "dotted";
        } else {
            style = edge.isDirect() ? "bold" : "dashed";
        }
        attributes.put("style", style);
        return attributes;
    }
}
