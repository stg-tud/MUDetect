package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.*;

import java.io.StringWriter;
import java.io.Writer;

public class AUGDotExporter {
    private final IntegerNameProvider<EGroumNode> nodeIdProvider = new IntegerNameProvider<>();
    private final DOTExporter<EGroumNode, EGroumEdge> exporter;

    public AUGDotExporter(AUGNodeAttributeProvider nodeAttributeProvider,
                          AUGEdgeAttributeProvider edgeAttributeProvider) {
        exporter = new DOTExporter<>(nodeIdProvider,
                EGroumNode::getLabel, EGroumEdge::getLabel,
                nodeAttributeProvider, edgeAttributeProvider);
    }

    public String toDotGraph(AUG aug) {
        StringWriter writer = new StringWriter();
        toDotGraph(aug, writer);
        return writer.toString();
    }

    void toDotGraph(AUG aug, Writer writer) {
        nodeIdProvider.clear();
        exporter.export(new GraphNameCorrectingPrintWriter(writer, aug.getLocation().getMethodName()), aug);
    }
}
