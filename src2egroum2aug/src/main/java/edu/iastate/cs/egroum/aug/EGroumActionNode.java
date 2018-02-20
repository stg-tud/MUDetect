package edu.iastate.cs.egroum.aug;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;

import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.THROW;

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

	public EGroumActionNode(String name, int operationType) {
		super(null, operationType, null);
		this.name = name;
	}

	@Override
	public String getLabel() {
		return dataType == null ? name : dataType;
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
	public Optional<String> getAPI() {
		if (getLabel().endsWith("()") || getLabel().endsWith("<init>")) {
			return Optional.of(getLabel().split("\\.")[0]);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public boolean hasBackwardDataDependence(EGroumActionNode preNode) {
		if (hasDataDependencyViaActionsOn(preNode)
				|| hasControlDependencyThatHasDataDependencyViaActionsOn(preNode)) {
			return true;
		}
		HashSet<EGroumNode> defs = new HashSet<>(), preDefs = new HashSet<>();
		getDefinitions(defs);
		preNode.getDefinitions(preDefs);
		return overlap(defs, preDefs);
	}

	private boolean hasDataDependencyViaActionsOn(EGroumActionNode preNode) {
		for (EGroumEdge edge : inEdges) {
			if (edge instanceof EGroumDataEdge) {
				if ((edge.source == preNode) ||
						(edge.source instanceof EGroumActionNode && ((EGroumActionNode) edge.source).hasDataDependencyViaActionsOn(preNode))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasControlDependencyThatHasDataDependencyViaActionsOn(EGroumActionNode preNode) {
		for (EGroumEdge edge : inEdges) {
			if (edge instanceof EGroumControlEdge) {
				for (EGroumEdge preEdge : edge.source.inEdges) {
					if (preEdge instanceof EGroumDataEdge && preEdge.source instanceof EGroumActionNode
							&& ((EGroumActionNode) preEdge.source).hasDataDependencyViaActionsOn(preNode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean hasBackwardDataDependence(EGroumNode node) {
		if (node instanceof EGroumActionNode)
			return hasBackwardDataDependence((EGroumActionNode) node);
		if (node instanceof EGroumDataNode) {
			HashSet<EGroumNode> defs = new HashSet<>(), preDefs = new HashSet<>();
			getDefinitions(defs);
			preDefs.addAll(node.getDefinitions());
			for (EGroumNode def : new HashSet<EGroumNode>(preDefs)) {
				EGroumNode qual = def.getQualifier();
				if (qual != null) {
					ArrayList<EGroumNode> tmpDefs = qual.getDefinitions();
					preDefs.addAll(tmpDefs);
				}
			}
			return overlap(defs, preDefs);
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
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == THROW) {
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

	private void getDefinitions(HashSet<EGroumNode> defs) {
		for (EGroumEdge e : inEdges) {
			if (e.source instanceof EGroumDataNode) {
				defs.addAll(e.source.getDefinitions());
				for (EGroumNode def : new HashSet<>(defs)) {
					EGroumNode qual = def.getQualifier();
					if (qual != null)
						defs.addAll(qual.getDefinitions());
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
							EGroumDataEdge de = (EGroumDataEdge) e1;
							/*if (this.hasBackwardDataDependence(e1.source))
								new EGroumDataEdge(e1.source, this, ((EGroumDataEdge) e1).type);
							else if (this.hasBackwardThrowDependence(e1.source))
								new EGroumDataEdge(e1.source, this, ((EGroumDataEdge) e1).type);*/
//							if (de.type == Type.FINALLY || e1.isDirect() || !e1.source.isCoreAction())
								new EGroumDataEdge(e1.source, this, de.type, de.label);
						}
					}
				}
			}
		}
		doneNodes.add(this);
	}
}
