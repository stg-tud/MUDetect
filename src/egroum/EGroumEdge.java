package egroum;

public abstract class EGroumEdge {
	protected EGroumNode source;
	protected EGroumNode target;
	
	public EGroumEdge(EGroumNode source, EGroumNode target) {
		this.source = source;
		this.target = target;
	}

	public abstract String getLabel();

	public EGroumNode getSource() {
		return source;
	}

	public EGroumNode getTarget() {
		return target;
	}

	public abstract String getExasLabel();
}
