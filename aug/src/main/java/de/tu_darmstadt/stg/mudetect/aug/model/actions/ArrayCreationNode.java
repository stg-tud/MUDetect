package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayCreationNode extends ConstructorCallNode {
    public ArrayCreationNode(String baseType) {
        super("{" + baseType + "}");
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
