package de.tu_darmstadt.stg.mudetect.model;

import egroum.*;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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

    private static class AUGNodeAttributeProvider implements ComponentAttributeProvider<EGroumNode> {
        @Override
        public Map<String, String> getComponentAttributes(EGroumNode node) {
            final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
            if (node instanceof EGroumActionNode) {
                attributes.put("shape", "box");
            } else if (node instanceof EGroumDataNode) {
                attributes.put("shape", "ellipse");
            }
            return attributes;
        }
    }

    private static class ViolationNodeAttributeProvider extends AUGNodeAttributeProvider {
        private final Instance instance;
        private final String unmappedNodeColor;

        private ViolationNodeAttributeProvider(Instance instance, String unmappedNodeColor) {
            this.instance = instance;
            this.unmappedNodeColor = unmappedNodeColor;
        }

        @Override
        public Map<String, String> getComponentAttributes(EGroumNode node) {
            final Map<String, String> attributes = super.getComponentAttributes(node);
            if (!instance.mapsPatternNode(node)) {
                attributes.put("color", unmappedNodeColor);
                attributes.put("fontcolor", unmappedNodeColor);
            }
            return attributes;
        }
    }

    private static class AUGEdgeAttributeProvider implements ComponentAttributeProvider<EGroumEdge> {
        @Override
        public Map<String, String> getComponentAttributes(EGroumEdge edge) {
            final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
            if (edge instanceof EGroumDataEdge) {
                attributes.put("style", "dotted");
            }
            return attributes;
        }
    }

    private static class ViolationEdgeAttributeProvider extends AUGEdgeAttributeProvider {
        private final Instance instance;
        private final String unmappedNodeColor;

        private ViolationEdgeAttributeProvider(Instance instance, String unmappedNodeColor) {
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

    private final Instance instance;
    private float confidence;

    private final DOTExporter<EGroumNode, EGroumEdge> violationDotExporter;
    private final DOTExporter<EGroumNode, EGroumEdge> targetDotExporter;

    public Violation(Instance overlap, float confidence) {
        this.instance = overlap;
        this.confidence = confidence;

        this.violationDotExporter = new DOTExporter<>(nodeIdProvider,
                EGroumNode::getLabel,
                EGroumEdge::getLabel,
                new ViolationNodeAttributeProvider(this.instance, "red"),
                new ViolationEdgeAttributeProvider(this.instance, "red"));
        this.targetDotExporter = new DOTExporter<>(nodeIdProvider,
                EGroumNode::getLabel,
                EGroumEdge::getLabel,
                new ViolationNodeAttributeProvider(this.instance, "gray"),
                new ViolationEdgeAttributeProvider(this.instance, "gray"));
    }

    public String toDotGraph() {
        StringWriter writer = new StringWriter();
        toDotGraph(writer);
        return writer.toString();
    }

    private void toDotGraph(Writer writer) {
        nodeIdProvider.clear();
        violationDotExporter.export(
                new GraphNameCorrectingPrintWriter(writer, instance),
                instance.getPattern().getAUG());
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

    public Instance getInstance() {
        return instance;
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
