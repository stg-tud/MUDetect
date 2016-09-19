package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Violation {
    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();
    private DOTExporter<EGroumNode, EGroumEdge> dotExporter =
            new DOTExporter<>(nodeIdProvider, EGroumNode::getLabel, EGroumEdge::getLabel, this::getAttributes, this::getAttributes);

    private Instance instance;

    public Violation(Instance overlap) {
        this.instance = overlap;
    }

    public Instance getInstance() {
        return instance;
    }

    public void toDotGraph(Writer writer) {
        nodeIdProvider.clear();
        dotExporter.export(new PrintWriter(writer) {
            @Override
            public void write(String s, int off, int len) {
                if (s.equals("digraph G {")) {
                    s = "digraph \"" + instance.getTarget().getName() + "\" {";
                }
                super.write(s, 0, s.length());
            }
        }, this.instance.getBase());
    }

    private Map<String, String> getAttributes(EGroumNode node) {
        Map<String, String> attributes = new HashMap<>();
        if (!this.instance.containsVertex(node)) {
            attributes.put("missing", "true");
            attributes.put("color", "red");
        }
        return attributes;
    }

    private Map<String, String> getAttributes(EGroumEdge edge) {
        Map<String, String> attributes = new HashMap<>();
        if (!this.instance.containsEdge(edge)) {
            attributes.put("missing", "true");
            attributes.put("color", "red");
        }
        return attributes;
    }
}