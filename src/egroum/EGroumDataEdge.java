package egroum;

public class EGroumDataEdge extends EGroumEdge {
	public enum Type {RECEIVER, PARAMETER, DEFINITION, REFERENCE, CONDITION, DEPENDENCE, QUALIFIER, THROW, FINALLY, ORDER}
	
	protected Type type;

	public EGroumDataEdge(EGroumNode source, EGroumNode target, Type type) {
		super(source, target);
		this.type = type;
		this.source.addOutEdge(this);
		this.target.addInEdge(this);
	}

	public Type getType() {
		return type;
	}

	@Override
	public String getLabel() {
		return getLabel(type);
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
		default: return "";
		}
	}

	@Override
	public String getExasLabel() {
		return getExasLabel(type);
	}
	
	public static String getExasLabel(Type type) {
		switch (type) {
		case RECEIVER: return "_recv_";
		case PARAMETER: return "_para_";
		case DEFINITION: return "_def_";
		case REFERENCE: return "_ref_";
		case CONDITION: return "_cond_";
		case DEPENDENCE: return "_dep_";
		case QUALIFIER: return "_qual_";
		case THROW: return "_throw_";
		case FINALLY: return "_final_";
		case ORDER: return "_order_";
		default: return "_data_";
		}
	}
}
