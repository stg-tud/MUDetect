package de.tu_darmstadt.stg.mudetect.aug.dot;

public class DisplayAUGDotExporter extends AUGDotExporter {
    public DisplayAUGDotExporter() {
        super(new AUGNodeLabelProvider(), new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
    }
}
