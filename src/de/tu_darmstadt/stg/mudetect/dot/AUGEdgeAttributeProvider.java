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
        if (edge instanceof EGroumDataEdge) {
            attributes.put("style", "dotted");
        }
        return attributes;
    }
}
