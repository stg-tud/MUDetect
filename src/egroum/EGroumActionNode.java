package egroum;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

public class EGroumActionNode extends EGroumNode {
	public static final String RECURSIVE = "recur";
	protected String name;
	protected String[] parameterTypes;
	protected HashSet<String> exceptionTypes;

	public EGroumActionNode(EGroumNode control, String branch, ASTNode astNode, int nodeType, String key, String type, String name) {
		super(astNode, nodeType, key);
		if (control != null) {
			this.control = control;
			new EGroumControlEdge(control, this, branch);
		}
		this.dataType = type;
		this.name = name;
	}

	public EGroumActionNode(EGroumNode control, String branch, ASTNode astNode, int nodeType, String key, String type, String name, HashSet<String> exceptions) {
		this(control, branch, astNode, nodeType, key, type, name);
		this.exceptionTypes = exceptions;
	}

	@Override
	public String getLabel() {
		return dataType == null ? name : dataType;
	}

	@Override
	public String getExasLabel() {
		//return name + (parameterTypes == null ? "" : buildParameters());
		int index = name.lastIndexOf('.');
		return name.substring(index + 1);
	}

	public String buildParameters() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if (parameterTypes.length > 0) {
			sb.append(parameterTypes[0]);
			for (int i = 1; i < parameterTypes.length; i++)
				sb.append("," + parameterTypes[i]);
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public boolean isSame(EGroumNode node) {
		if (node instanceof EGroumActionNode) {
			return name.equals(((EGroumActionNode) node).name);
		}
		return false;
	}
	
	@Override
	public boolean isDefinition() {
		return false;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}

	public boolean hasBackwardDataDependence(EGroumActionNode preNode) {
		HashSet<EGroumNode> defs = new HashSet<>(), preDefs = new HashSet<>();
		HashSet<String> fields = new HashSet<>(), preFields = new HashSet<>();
		getDefinitions(defs, fields);
		preNode.getDefinitions(preDefs, preFields);
		return (overlap(defs, preDefs) || overlap(fields, preFields));
	}

	public boolean hasBackwardDataDependence(EGroumNode node) {
		if (node instanceof EGroumActionNode)
			return hasBackwardDataDependence((EGroumActionNode) node);
		if (node instanceof EGroumDataNode) {
			HashSet<EGroumNode> defs = new HashSet<>(), preDefs = new HashSet<>();
			HashSet<String> fields = new HashSet<>(), preFields = new HashSet<>();
			getDefinitions(defs, fields);
			preDefs.addAll(node.getDefinitions());
			if (preDefs.isEmpty())
				preFields.add(node.key);
			return (overlap(defs, preDefs) || overlap(fields, preFields));
		}
		return false;
	}

	private <E> boolean overlap(HashSet<E> s1, HashSet<E> s2) {
		HashSet<E> c = new HashSet<>(s1);
		c.retainAll(s2);
		return !c.isEmpty();
	}

	private void getDefinitions(HashSet<EGroumNode> defs, HashSet<String> fields) {
		for (EGroumEdge e : inEdges) {
			if (e.source instanceof EGroumDataNode) {
				ArrayList<EGroumNode> tmpDefs = e.source.getDefinitions();
				if (tmpDefs.isEmpty())
					fields.add(e.source.key);
				else
					defs.addAll(tmpDefs);
			}
		}
	}
}
