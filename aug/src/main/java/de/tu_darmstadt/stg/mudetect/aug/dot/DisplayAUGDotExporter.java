package de.tu_darmstadt.stg.mudetect.aug.dot;

import de.tu_darmstadt.stg.mudetect.aug.Node;

public class DisplayAUGDotExporter extends AUGDotExporter {
    public DisplayAUGDotExporter() {
        super(Node::getLabel, new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
    }
}
