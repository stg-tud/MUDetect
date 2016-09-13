package egroum;

public class EGroumControlEdge extends EGroumEdge {

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
}
