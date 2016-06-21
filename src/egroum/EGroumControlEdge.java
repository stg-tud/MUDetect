package egroum;

public class EGroumControlEdge extends EGroumEdge {
	protected String label;

	public EGroumControlEdge(EGroumNode point, EGroumNode next, String label) {
		super(point, next);
		this.label = label;
		this.source.addOutEdge(this);
		this.target.addInEdge(this);
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	@Override
	public String getExasLabel() {
		return "_control_";
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
}
