package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumNode;

public class AUGTestUtils {
    public static AUG createAUG(EGroumNode... nodes) {
        AUG aug = new AUG();
        for (EGroumNode node : nodes) {
            aug.addVertex(node);
        }
        return aug;
    }
}
