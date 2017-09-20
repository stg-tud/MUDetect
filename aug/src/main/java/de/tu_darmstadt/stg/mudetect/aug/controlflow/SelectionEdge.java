package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.Node;

public class SelectionEdge extends ConditionEdge {
    public SelectionEdge(Node source, Node target) {
        super(source, target, ConditionType.SELECTION);
    }
}
