package de.tu_darmstadt.stg.mudetect.aug;

abstract class InvokationNode extends BaseNode implements ActionNode {
    @Override
    public boolean isCoreAction() {
        return true;
    }
}
