package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Violation implements Comparable<Violation> {
    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();
    private DOTExporter<EGroumNode, EGroumEdge> dotExporter =
            new DOTExporter<>(nodeIdProvider, EGroumNode::getLabel, EGroumEdge::getLabel, this::getAttributes, this::getAttributes);

    private Instance instance;
    private float confidence;

    public Violation(Instance overlap, float confidence) {
        this.instance = overlap;
        this.confidence = confidence;
    }

    public String toDotGraph() {
        StringWriter writer = new StringWriter();
        toDotGraph(writer);
        return writer.toString();
    }

    public void toDotGraph(Writer writer) {
        nodeIdProvider.clear();
        dotExporter.export(new PrintWriter(writer) {
            @Override
            public void write(String s, int off, int len) {
                if (s.equals("digraph G {")) {
                    s = "digraph \"" + instance.getLocation().getMethodName() + "\" {";
                }
                super.write(s, 0, s.length());
            }
        }, this.instance.getPattern());
    }

    private Map<String, String> getAttributes(EGroumNode node) {
        Map<String, String> attributes = new LinkedHashMap<>();
        if (!this.instance.mapsPatternNode(node)) {
            attributes.put("missing", "true");
            attributes.put("color", "red");
            attributes.put("fontcolor", "red");
        }
        return attributes;
    }

    private Map<String, String> getAttributes(EGroumEdge edge) {
        Map<String, String> attributes = new LinkedHashMap<>();
        if (!this.instance.mapsPatternEdge(edge)) {
            attributes.put("missing", "true");
            attributes.put("color", "red");
            attributes.put("fontcolor", "red");
        }
        return attributes;
    }

    public Location getLocation() {
        return instance.getLocation();
    }

    public float getConfidence() {
        return confidence;
    }

    @Override
    public int compareTo(Violation o) {
        return Float.compare(getConfidence(), o.getConfidence());
    }

    @Override
    public String toString() {
        return toDotGraph();
    }
}
