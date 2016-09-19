package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Violation {
    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();
    private DOTExporter<EGroumNode, EGroumEdge> dotExporter =
            new DOTExporter<>(nodeIdProvider, EGroumNode::getLabel, EGroumEdge::getLabel, this::getAttributes, null);

    private Instance instance;

    public Violation(Instance overlap) {
        this.instance = overlap;
    }

    public Instance getInstance() {
        return instance;
    }

    public void toDotGraph(StringWriter writer) {
        nodeIdProvider.clear();
        dotExporter.export(writer, this.instance.getBase());
    }

    private Map<String, String> getAttributes(EGroumNode node) {
        Map<String, String> attributes = new HashMap<>();
        if (!this.instance.containsVertex(node)) {
            attributes.put("missing", "true");
            attributes.put("color", "red");
        }
        return attributes;
    }
}
