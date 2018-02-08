package edu.iastate.cs.egroum.aug;

import java.util.HashSet;

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

	@Override
	public boolean isDirect() {
		HashSet<EGroumNode> inter = new HashSet<>();
		for (EGroumEdge e: this.source.outEdges)
			if (e instanceof EGroumControlEdge)
				inter.add(e.target);
		for (EGroumEdge e : this.target.inEdges) {
			if (e instanceof EGroumControlEdge && inter.contains(e.source))
				return false;
		}
		return true;
	}
}
