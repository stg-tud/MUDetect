package edu.iastate.cs.egroum.aug;

import java.util.HashSet;

public class EGroumDataEdge extends EGroumEdge {
	public enum Type {RECEIVER, PARAMETER, DEFINITION, REFERENCE, CONDITION, DEPENDENCE, QUALIFIER, THROW, FINALLY, ORDER, CONTAINS}
	
	protected Type type;

	public EGroumDataEdge(EGroumNode source, EGroumNode target, Type type) {
		super(source, target);
		this.type = type;
		this.source.addOutEdge(this);
		this.target.addInEdge(this);
	}

	public EGroumDataEdge(EGroumNode source, EGroumNode target, Type type, String label) {
		this(source, target, type);
		this.label = label;
	}

	public EGroumDataEdge(EGroumNode source, EGroumNode target, Type type, String label, boolean transitive) {
		this(source, target, type, label);
		this.isTransitive = transitive;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String getLabel() {
		return getLabel(type, label);
	}

	@Override
	public boolean isDirect() {
		HashSet<EGroumNode> inter = new HashSet<>();
		for (EGroumEdge e: this.source.outEdges)
			if (e instanceof EGroumDataEdge && (!(this.source instanceof EGroumDataNode) || e.target instanceof EGroumDataNode))
				inter.add(e.target);
		for (EGroumEdge e : this.target.inEdges) {
			if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == this.type && inter.contains(e.source))
				return false;
		}
		return true;
	}

	public static String getLabel(Type type) {
		switch (type) {
		case RECEIVER: return "recv";
		case PARAMETER: return "para";
		case DEFINITION: return "def";
		case REFERENCE: return "ref";
		case CONDITION: return "cond";
		case DEPENDENCE: return "dep";
		case QUALIFIER: return "qual";
		case THROW: return "throw";
		case FINALLY: return "final";
		case ORDER: return "order";
		case CONTAINS: return "contains";
		default: return "";
		}
	}

	public static String getLabel(Type type, String label) {
		switch (type) {
		case RECEIVER: return "recv";
		case PARAMETER: return "para";
		case DEFINITION: return "def";
		case REFERENCE: return "ref";
		case CONDITION: return label == null ? "cond" : label;
		case DEPENDENCE: return "dep";
		case QUALIFIER: return "qual";
		case THROW: return "throw";
		case FINALLY: return "final";
		case ORDER: return "order";
		case CONTAINS: return "contains";
		default: return "";
		}
	}
}
