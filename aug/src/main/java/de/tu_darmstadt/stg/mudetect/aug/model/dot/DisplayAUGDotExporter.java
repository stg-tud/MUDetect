package de.tu_darmstadt.stg.mudetect.aug.model.dot;

import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.WithSourceLineNumberLabelProvider;

public class DisplayAUGDotExporter extends AUGDotExporter {
    public DisplayAUGDotExporter() {
        super(new WithSourceLineNumberLabelProvider(new BaseAUGLabelProvider()),
                new AUGNodeAttributeProvider(),
                new AUGEdgeAttributeProvider());
    }
}
