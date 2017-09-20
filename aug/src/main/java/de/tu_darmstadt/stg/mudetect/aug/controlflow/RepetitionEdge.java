package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.Node;

public class RepetitionEdge extends ConditionEdge {
    public RepetitionEdge(Node source, Node target) {
        super(source, target, ConditionType.REPETITION);
    }
}
