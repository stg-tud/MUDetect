package egroum;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

import egroum.EGroumDataEdge.Type;

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

	public EGroumActionNode(EGroumActionNode node) {
		super(node.astNode, node.astNodeType, node.key);
		if (node.control != null) {
			this.control = node.control;
		}
		this.dataType = node.dataType;
		this.name = node.name;
		this.exceptionTypes = node.exceptionTypes;
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
			EGroumNode qual = node.getQualifier();
			if (qual != null) {
				ArrayList<EGroumNode> tmpDefs = qual.getDefinitions();
				if (tmpDefs.isEmpty())
					preFields.add(qual.key);
				else
					preDefs.addAll(tmpDefs);
			}
			return (overlap(defs, preDefs) || overlap(fields, preFields));
		}
		return false;
	}

	public boolean hasBackwardThrowDependence(EGroumNode node) {
		if (node instanceof EGroumDataNode) {
			ArrayList<EGroumNode> preDefs = node.getDefinitions();
			if (preDefs.isEmpty())
				preDefs.add(node);
			for (EGroumNode predef : preDefs) {
				for (EGroumEdge e : predef.inEdges) {
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.THROW) {
						if (hasBackwardDataDependence(e.source))
							return true;
					}
				}
			}
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
				EGroumNode qual = e.source.getQualifier();
				if (qual != null) {
					tmpDefs = qual.getDefinitions();
					if (tmpDefs.isEmpty())
						fields.add(qual.key);
					else
						defs.addAll(tmpDefs);
				}
			}
		}
	}
	
	@Override
	public void buildControlClosure(HashSet<EGroumNode> doneNodes) {
		for (EGroumEdge e : new HashSet<EGroumEdge>(this.getInEdges())) {
			if (e instanceof EGroumControlEdge) {
				EGroumNode inNode = e.source;
				if (!doneNodes.contains(inNode))
					inNode.buildControlClosure(doneNodes);
				for (EGroumEdge e1 : inNode.inEdges) {
					if (!this.hasInEdge(e1)) {
						if (e1 instanceof EGroumControlEdge)
							new EGroumControlEdge(e1.source, this, ((EGroumControlEdge) e1).label);
						else {
							if (this.hasBackwardDataDependence(e1.source))
								new EGroumDataEdge(e1.source, this, ((EGroumDataEdge) e1).type);
							else if (this.hasBackwardThrowDependence(e1.source))
								new EGroumDataEdge(e1.source, this, ((EGroumDataEdge) e1).type);
						}
					}
				}
			}
		}
	}
}
