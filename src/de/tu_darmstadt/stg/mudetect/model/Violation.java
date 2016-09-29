package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Violation implements Comparable<Violation> {

    private static class GraphNameCorrectingPrintWriter extends PrintWriter {
        private final Instance instance;

        GraphNameCorrectingPrintWriter(Writer writer, Instance instance) {
            super(writer);
            this.instance = instance;
        }

        @Override
        public void write(String s, int off, int len) {
            if (s.equals("digraph G {")) {
                s = "digraph \"" + instance.getLocation().getMethodName() + "\" {";
            }
            super.write(s, 0, s.length());
        }
    }

    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();

    private final Map<String, String> noAttributes = Collections.emptyMap();

    private final Map<String, String> missingElementAttributes = new LinkedHashMap<String, String>() {{
        put("missing", "true");
        put("color", "red");
        put("fontcolor", "red");
    }};

    private final DOTExporter<EGroumNode, EGroumEdge> violationDotExporter =
            new DOTExporter<>(nodeIdProvider,
                    EGroumNode::getLabel,
                    EGroumEdge::getLabel,
                    node -> !this.instance.mapsPatternNode(node) ? this.missingElementAttributes : this.noAttributes,
                    edge -> !this.instance.mapsPatternEdge(edge) ? this.missingElementAttributes : this.noAttributes);

    private final Map<String, String> mappedElementAttributes = new LinkedHashMap<String, String>() {{
        put("mapped", "true");
        put("color", "blue");
        put("fontcolor", "blue");
    }};

    private final DOTExporter<EGroumNode, EGroumEdge> targetDotExporter =
            new DOTExporter<>(nodeIdProvider,
                    EGroumNode::getLabel,
                    EGroumEdge::getLabel,
                    node -> this.instance.mapsPatternNode(node) ? this.mappedElementAttributes : this.noAttributes,
                    edge -> this.instance.mapsPatternEdge(edge) ? this.mappedElementAttributes : this.noAttributes);

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

    private void toDotGraph(Writer writer) {
        nodeIdProvider.clear();
        violationDotExporter.export(new GraphNameCorrectingPrintWriter(writer, instance), instance.getPattern());
    }

    public String toTargetDotGraph() {
        StringWriter writer = new StringWriter();
        toTargetDotGraph(writer);
        return writer.toString();
    }

    private void toTargetDotGraph(Writer writer) {
        nodeIdProvider.clear();
        targetDotExporter.export(new GraphNameCorrectingPrintWriter(writer, instance), instance.getTarget());
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
