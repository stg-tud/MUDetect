package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

import java.io.StringWriter;

public class Violation {
    private static final DOTExporter<EGroumNode, EGroumEdge> dotGraphExporter =
            new DOTExporter<>(new IntegerNameProvider<>(), EGroumNode::getLabel, null);

    private Instance instance;

    public Violation(Instance overlap) {
        this.instance = overlap;
    }

    public Instance getInstance() {
        return instance;
    }

    public void toDotGraph(StringWriter writer) {
        dotGraphExporter.export(writer, this.instance);
    }
}
