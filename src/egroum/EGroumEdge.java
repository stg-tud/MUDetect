package egroum;

import egroum.EGroumDataEdge.Type;

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

	public boolean isParameter() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).type == Type.PARAMETER;
	}

	public boolean isDef() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).type == Type.DEFINITION;
	}

	public void delete() {
		this.source.outEdges.remove(this);
		this.target.inEdges.remove(this);
		// FIXME
		/*this.source = null;
		this.target = null;*/
	}

	public static void createEdge(EGroumNode source, EGroumNode target, EGroumEdge e) {
		if (e instanceof EGroumDataEdge)
			new EGroumDataEdge(source, target, ((EGroumDataEdge) e).type);
		if (e instanceof EGroumControlEdge)
			new EGroumControlEdge(source, target, ((EGroumControlEdge) e).label);
	}
}
