package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayCreationNode extends ConstructorCallNode {
    public ArrayCreationNode(String baseType) {
        super("{" + baseType + "}");
    }
}
