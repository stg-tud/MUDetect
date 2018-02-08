package edu.iastate.cs.egroum.aug;

import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.*;

public abstract class EGroumEdge {
	protected int id;
	protected EGroumNode source;
	protected EGroumNode target;
	protected String label;
	protected boolean isTransitive = false;
	
	public EGroumEdge(EGroumNode source, EGroumNode target) {
		this.source = source;
		this.target = target;
	}

	public int getId() {
		return id;
	}

	public abstract String getLabel();

	public EGroumNode getSource() {
		return source;
	}

	public EGroumNode getTarget() {
		return target;
	}

	public boolean isParameter() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).type == PARAMETER;
	}

	public boolean isDef() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).getType() == DEFINITION;
	}

	public boolean isRecv() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).getType() == RECEIVER;
	}

	public boolean isCond() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).getType() == CONDITION;
	}

	public boolean isThrow() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).getType() == THROW;
	}

	public boolean isSync() {
		return this instanceof EGroumDataEdge && ((EGroumDataEdge) this).getType() == CONDITION && getLabel().equals("syn");
	}

	public void delete() {
		this.source.outEdges.remove(this);
		this.target.inEdges.remove(this);
		this.source = null;
		this.target = null;
	}

	public static void createEdge(EGroumNode source, EGroumNode target, EGroumEdge e) {
		if (e instanceof EGroumDataEdge)
			new EGroumDataEdge(source, target, ((EGroumDataEdge) e).type, ((EGroumDataEdge) e).label);
		if (e instanceof EGroumControlEdge)
			new EGroumControlEdge(source, target, ((EGroumControlEdge) e).label);
	}

	abstract public boolean isDirect();
	
	@Override
	public String toString() {
		return source + "-" + getLabel() +"->" + target;
	}

	public boolean isExceptionHandling() {
		return label.equals("hdl");
	}
}
