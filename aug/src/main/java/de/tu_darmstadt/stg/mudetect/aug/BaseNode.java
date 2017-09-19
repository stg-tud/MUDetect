package de.tu_darmstadt.stg.mudetect.aug;

public abstract class BaseNode implements Node, NodeWithLocation {
    private static int nextNodeId = 0;

    private final int id;
    private final int sourceLineNumber;
    private APIUsageGraph aug;

    protected BaseNode() { this(-1); }

    protected BaseNode(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
        this.id = nextNodeId++;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setGraph(APIUsageGraph aug) {
        this.aug = aug;
    }

    @Override
    public APIUsageGraph getGraph() {
        return aug;
    }

    @Override
    public int getSourceLineNumber() {
        return sourceLineNumber;
    }

    @Override
    public String toString() {
        String type = getClass().getSimpleName();
        if (type.endsWith("Node")) {
            type = type.substring(0, type.length() - 4);
        }
        return type + ":" + getLabel();
    }
}
