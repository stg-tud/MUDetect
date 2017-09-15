package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.ASTNode;
import org.jgrapht.ext.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AUGDotExporter {
    private static final String NEW_LINE = System.getProperty("line.separator");

    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();
    private final DOTExporter<EGroumNode, EGroumEdge> exporter;

    public AUGDotExporter(VertexNameProvider<EGroumNode> nodeLabelProvider,
                          AUGNodeAttributeProvider nodeAttributeProvider,
                          AUGEdgeAttributeProvider edgeAttributeProvider) {
        exporter = new DOTExporter<>(nodeIdProvider,
                nodeLabelProvider, EGroumEdge::getLabel,
                nodeAttributeProvider, edgeAttributeProvider);
    }

    public String toDotGraph(AUG aug) {
        return toDotGraph(aug, new HashMap<>());
    }

    public String toDotGraph(AUG aug, Map<String, String> graphAttributes) {
        StringWriter writer = new StringWriter();
        toDotGraph(aug, graphAttributes, writer);
        return writer.toString();
    }

    private void toDotGraph(AUG aug, Map<String, String> graphAttributes, Writer writer) {
        nodeIdProvider.clear();
        exporter.export(new PrintWriter(writer) {
            @Override
            public void write(String s, int off, int len) {
                if (s.equals("digraph G {")) {
                    String methodName = aug.getLocation().getMethodName();
                    StringBuilder data = new StringBuilder("digraph \"").append(methodName).append("\" {").append(NEW_LINE);
                    for (Map.Entry<String, String> attribute : graphAttributes.entrySet()) {
                        data.append(attribute.getKey()).append("=").append(attribute.getValue()).append(";").append(NEW_LINE);
                    }
                    super.write(data.toString(), 0, data.length());
                } else {
                    super.write(s, off, len);
                }
            }
        }, aug);
    }
}
