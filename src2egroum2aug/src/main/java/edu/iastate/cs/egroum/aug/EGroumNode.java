package edu.iastate.cs.egroum.aug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;

import edu.iastate.cs.egroum.utils.JavaASTUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.*;

public abstract class EGroumNode {
	protected static final String PREFIX_DUMMY = "dummy_";
	public static int numOfNodes = 0;
	private static HashSet<Integer> invocationTypes = new HashSet<>(), controlTypes = new HashSet<>(), literalTypes = new HashSet<>();

	static {
		/*invocationTypes.add(ASTNode.ARRAY_ACCESS);
		invocationTypes.add(ASTNode.ARRAY_CREATION);
		invocationTypes.add(ASTNode.ARRAY_INITIALIZER);*/
//		invocationTypes.add(ASTNode.ASSERT_STATEMENT);
		invocationTypes.add(ASTNode.ASSIGNMENT);
		invocationTypes.add(ASTNode.BREAK_STATEMENT);
//		invocationTypes.add(ASTNode.CAST_EXPRESSION);
		invocationTypes.add(ASTNode.CLASS_INSTANCE_CREATION);
		invocationTypes.add(ASTNode.CONSTRUCTOR_INVOCATION);
		invocationTypes.add(ASTNode.CONTINUE_STATEMENT);
//		invocationTypes.add(ASTNode.INSTANCEOF_EXPRESSION);
		invocationTypes.add(ASTNode.METHOD_INVOCATION);
		invocationTypes.add(ASTNode.RETURN_STATEMENT);
		invocationTypes.add(ASTNode.SUPER_CONSTRUCTOR_INVOCATION);
		invocationTypes.add(ASTNode.SUPER_METHOD_INVOCATION);
		invocationTypes.add(ASTNode.THROW_STATEMENT);
		
		controlTypes.add(ASTNode.CATCH_CLAUSE);
		controlTypes.add(ASTNode.DO_STATEMENT);
		controlTypes.add(ASTNode.ENHANCED_FOR_STATEMENT);
		controlTypes.add(ASTNode.FOR_STATEMENT);
		controlTypes.add(ASTNode.IF_STATEMENT);
		controlTypes.add(ASTNode.SWITCH_STATEMENT);
		controlTypes.add(ASTNode.SYNCHRONIZED_STATEMENT);
		controlTypes.add(ASTNode.TRY_STATEMENT);
		controlTypes.add(ASTNode.WHILE_STATEMENT);
		
		literalTypes.add(ASTNode.BOOLEAN_LITERAL);
		literalTypes.add(ASTNode.CHARACTER_LITERAL);
		literalTypes.add(ASTNode.NULL_LITERAL);
		literalTypes.add(ASTNode.NUMBER_LITERAL);
		literalTypes.add(ASTNode.STRING_LITERAL);
		literalTypes.add(ASTNode.TYPE_LITERAL);
	}
	
	protected int id;
	protected ASTNode astNode;
	protected int astNodeType;
	private int sourceLineNumber;
	protected String key;
	protected EGroumNode control;
	protected String dataType;
	protected EGroumGraph graph;
	protected ArrayList<EGroumEdge> inEdges = new ArrayList<EGroumEdge>();
	protected ArrayList<EGroumEdge> outEdges = new ArrayList<EGroumEdge>();
	protected HashMap<String, HashSet<EGroumDataNode>> defStore = new HashMap<>();

	public EGroumNode(ASTNode astNode, int nodeType) {
		this.id = ++numOfNodes;
		this.astNode = astNode;
		this.astNodeType = nodeType;
		this.sourceLineNumber = -1;
		ASTNode node = astNode;
		while (node != null) {
			if (node instanceof CompilationUnit) {
				this.sourceLineNumber = ((CompilationUnit) node).getLineNumber(astNode.getStartPosition());
			}
			node = node.getParent();
		}
	}
	
	public EGroumNode(ASTNode astNode, int nodeType, String key) {
		this(astNode, nodeType);
		this.key = key;
	}

	public int getId() {
		return id;
	}

	public EGroumGraph getGraph() {
		return graph;
	}

	public void setGraph(EGroumGraph groum) {
		this.graph = groum;
	}
	
	public String getDataType() {
		return dataType;
	}

	public String getDataName() {
		if (this instanceof EGroumDataNode)
			return ((EGroumDataNode) this).getDataName();
		return null;
	}
	
	abstract public String getLabel();
	
	public int getAstNodeType() {
		return astNodeType;
	}

	public ASTNode getAstNode() {
		return astNode;
	}

	public Optional<Integer> getSourceLineNumber() {
		return sourceLineNumber == -1 ? Optional.empty() : Optional.of(sourceLineNumber);
	}

	public ArrayList<EGroumEdge> getInEdges() {
		return inEdges;
	}

	public ArrayList<EGroumEdge> getOutEdges() {
		return outEdges;
	}

	public void addOutEdge(EGroumEdge edge) {
		outEdges.add(edge);
	}

	public void addInEdge(EGroumEdge edge) {
		inEdges.add(edge);
	}

	public boolean isLiteral() {
		return JavaASTUtil.isLiteral(astNodeType);
	}

	abstract public Optional<String> getAPI();

	public void delete() {
		for (EGroumEdge e : inEdges) {
			e.source.outEdges.remove(e);
		}
		inEdges.clear();
		for (EGroumEdge e : outEdges)
			e.target.inEdges.remove(e);
		outEdges.clear();
		control = null;
	}

	public boolean isDefinition() {
		if (this instanceof EGroumDataNode)
			return ((EGroumDataNode) this).isDefinition();
		return false;
	}

	public boolean isDeclaration() {
		return (this instanceof EGroumDataNode) && ((EGroumDataNode) this).isDeclaration;
	}

	public boolean isStatement() {
		return control != null;
	}

	public boolean isInfixOperator() {
		return getAstNodeType() == ASTNode.INFIX_EXPRESSION;
	}

	public ArrayList<EGroumNode> getIncomingEmptyNodes() {
		ArrayList<EGroumNode> nodes = new ArrayList<>();
		for (EGroumEdge e : inEdges)
			if (e.source.isEmptyNode())
				nodes.add(e.source);
		return nodes;
	}
	
	ArrayList<EGroumEdge> getInEdgesForExasVectorization() {
		ArrayList<EGroumEdge> edges = new ArrayList<>();
		for (EGroumEdge e : inEdges)
			if (!(e instanceof EGroumDataEdge) || ((EGroumDataEdge) e).type != DEPENDENCE)
				edges.add(e);
		return edges;
	}
	
	ArrayList<EGroumEdge> getOutEdgesForExasVectorization() {
		ArrayList<EGroumEdge> edges = new ArrayList<>();
		for (EGroumEdge e : outEdges)
			if (!(e instanceof EGroumDataEdge) || ((EGroumDataEdge) e).type != DEPENDENCE)
				edges.add(e);
		return edges;
	}

	public boolean isEmptyNode() {
		return this instanceof EGroumActionNode && ((EGroumActionNode) this).name.equals("empty");
	}

	private void adjustControl(EGroumNode empty, int index) {
		EGroumControlEdge e = getControlInEdge(control);
		control.outEdges.remove(e);
		e.source = empty.control;
		empty.control.outEdges.add(index, e);
		e.label = empty.getControlInEdge(empty.control).getLabel();
		control = empty.control;
	}

	public EGroumControlEdge getControlInEdge(EGroumNode node) {
		for (EGroumEdge e : inEdges)
			if (e.source == node && e instanceof EGroumControlEdge)
				return (EGroumControlEdge) e;
		return null;
	}

	public EGroumDataEdge getQualifierInEdge(EGroumNode node) {
		for (EGroumEdge e : inEdges)
			if (e.source == node && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == QUALIFIER)
				return (EGroumDataEdge) e;
		return null;
	}

	public void adjustControl(EGroumNode node, EGroumNode empty) {
		int i = 0;
		while (outEdges.get(i).target != node) {
			i++;
		}
		int index = empty.control.getOutEdgeIndex(empty);
		while (i < outEdges.size() && !outEdges.get(i).target.isEmptyNode()) {
			index++;
			outEdges.get(i).target.adjustControl(empty, index);
		}
	}

	public ArrayList<EGroumEdge> getInDependences() {
		ArrayList<EGroumEdge> es = new ArrayList<>();
		for (EGroumEdge e : inEdges)
			if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == DEPENDENCE)
				es.add(e);
		return es;
	}

	public int getOutEdgeIndex(EGroumNode node) {
		int i = 0;
		while (i < outEdges.size()) {
			if (outEdges.get(i).target == node)
				return i;
			i++;
		}
		return -1;
	}

	public boolean isSame(EGroumNode node) {
		if (key == null && node.key != null)
			return false;
		if (!key.equals(node.key))
			return false;
		if (this instanceof EGroumActionNode)
			return ((EGroumActionNode) this).isSame(node);
		if (this instanceof EGroumDataNode)
			return ((EGroumDataNode) this).isSame(node);
		if (this instanceof EGroumControlNode)
			return ((EGroumControlNode) this).isSame(node);
		return false;
	}

	public EGroumNode getQualifier() {
		if (this instanceof EGroumDataNode)
			return ((EGroumDataNode) this).getQualifier();
		return null;
	}

	public EGroumNode getDefinition() {
		if (this instanceof EGroumDataNode && this.inEdges.size() == 1 && this.inEdges.get(0) instanceof EGroumDataEdge) {
			EGroumDataEdge e = (EGroumDataEdge) this.inEdges.get(0);
			if (e.type == REFERENCE)
				return e.source;
		}
		return null;
	}

	/**
	 * The source nodes of all incoming REFERENCE edges, if this is a data node. Empty set otherwise.
	 */
	public ArrayList<EGroumNode> getDefinitions() {
		ArrayList<EGroumNode> defs = new ArrayList<>();
		if (this instanceof EGroumDataNode) {
			for (EGroumEdge e : this.inEdges) {
				if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == REFERENCE)
					defs.add(e.source);
			}
		}
		return defs;
	}

	public ArrayList<EGroumNode> getInitActions() {
		ArrayList<EGroumNode> initActions = new ArrayList<>();
		ArrayList<EGroumNode> defs = getDefinitions();
		for (EGroumNode def : defs) {
			for (EGroumEdge e1 : def.inEdges) {
				if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == DEFINITION) {
					for (EGroumEdge e2 : e1.source.inEdges) {
						if (e2 instanceof EGroumDataEdge && ((EGroumDataEdge) e2).type == PARAMETER) {
							if (e2.source instanceof EGroumActionNode)
								initActions.add(e2.source);
							else
								initActions.addAll(e2.source.getInitActions());
							break;
						}
					}
					break;
				}
			}
		}
		return initActions;
	}

	public HashSet<EGroumNode> getCatchClauses() {
		HashSet<EGroumNode> ccs = new HashSet<>();
		for (EGroumEdge e : inEdges) {
			if (e.source.astNodeType == ASTNode.CATCH_CLAUSE)
				ccs.add(e.source);
			if (e instanceof EGroumControlEdge)
				ccs.addAll(e.source.getCatchClauses());
		}
		return ccs;
	}

	public ArrayList<EGroumNode> getReferences() {
		ArrayList<EGroumNode> refs = new ArrayList<>();
		if (this instanceof EGroumDataNode) {
			for (EGroumEdge e : this.outEdges) {
				if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == REFERENCE)
					refs.add(e.target);
			}
		}
		return refs;
	}

	public boolean hasInEdge(EGroumEdge edge) {
		for (EGroumEdge e : inEdges)
			if (e.source == edge.source && e.getLabel().equals(edge.getLabel()))
				return true;
		return false;
	}

	public boolean hasInNode(EGroumNode preNode) {
		for (EGroumEdge e : inEdges)
			if (e.source == preNode && (!(e instanceof EGroumDataEdge) || ((EGroumDataEdge) e).type != DEPENDENCE))
				return true;
		return false;
	}

	public boolean hasInDataNode(EGroumNode node, EGroumDataEdge.Type type) {
		for (EGroumEdge e : inEdges)
			if (e instanceof EGroumDataEdge && e.source == node && ((EGroumDataEdge) e).type == type)
				return true;
		return false;
	}

	public boolean hasOutNode(EGroumNode target) {
		for (EGroumEdge e : outEdges)
			if (e.target == target)
				return true;
		return false;
	}

	public boolean isAssignment() {
		return this.getLabel().equals("=");
	}

	public HashSet<EGroumNode> getInNodes() {
		HashSet<EGroumNode> nodes = new HashSet<>();
		for (EGroumEdge e : this.inEdges)
			nodes.add(e.source);
		return nodes;
	}

	public HashSet<EGroumNode> getOutNodes() {
		HashSet<EGroumNode> nodes = new HashSet<>();
		for (EGroumEdge e : this.outEdges)
			nodes.add(e.target);
		return nodes;
	}

	public String getConditionLabel() {
		if (astNodeType == ASTNode.CONDITIONAL_EXPRESSION || astNodeType == ASTNode.IF_STATEMENT)
			return "sel";
		if (astNodeType == ASTNode.DO_STATEMENT || astNodeType == ASTNode.ENHANCED_FOR_STATEMENT || astNodeType == ASTNode.FOR_STATEMENT || astNodeType == ASTNode.WHILE_STATEMENT)
			return "rep";
		if (astNodeType == ASTNode.SYNCHRONIZED_STATEMENT)
			return "syn";
		if (astNodeType == ASTNode.CATCH_CLAUSE)
			return "hdl";
		return null;
	}

	public boolean isCoreAction() {
		if (this instanceof EGroumActionNode && ((EGroumActionNode) this).name.startsWith("get"))
			return false;
		return isCoreAction(astNodeType);
	}

	public static boolean isCoreAction(int astNodeType) {
		return invocationTypes.contains(astNodeType);
	}

	public boolean isMeaningfulAction() {
		return astNodeType == ASTNode.METHOD_INVOCATION || astNodeType == ASTNode.CONSTRUCTOR_INVOCATION;
	}

	HashSet<EGroumNode> buildTransitiveParameterClosure() {
		HashSet<EGroumNode> parameterTransitiveNodes = new HashSet<>();
		LinkedList<EGroumNode> nodes = new LinkedList<>();
		nodes.add(this);
		while (!nodes.isEmpty()) {
			EGroumNode node = nodes.removeFirst();
			parameterTransitiveNodes.add(node);
			for (EGroumEdge e : node.inEdges) {
				if (e instanceof EGroumDataEdge && !parameterTransitiveNodes.contains(e.source)) {
					EGroumDataEdge de = (EGroumDataEdge) e;
					if (de.type == PARAMETER || de.type == QUALIFIER || de.type == RECEIVER || de.type == REFERENCE || de.type == DEFINITION)
						nodes.add(de.source);
				}
			}
		}
		return parameterTransitiveNodes;
	}

	public void buildControlClosure(HashSet<EGroumNode> doneNodes) {
		if (this instanceof EGroumControlNode) {
			((EGroumControlNode) this).buildControlClosure(doneNodes);
		} else if (this instanceof EGroumActionNode)
			((EGroumActionNode) this).buildControlClosure(doneNodes);
		else
			doneNodes.add(this);
	}

	public void buildDataClosure(HashSet<EGroumNode> doneNodes) {
		if (this instanceof EGroumActionNode || this instanceof EGroumDataNode) {
			for (EGroumEdge e : new HashSet<EGroumEdge>(this.inEdges)) {
				if (!(e instanceof EGroumDataEdge))
					continue;
				EGroumDataEdge de = (EGroumDataEdge) e;
				if (e.source instanceof EGroumActionNode) {
					if (!doneNodes.contains(e.source))
						e.source.buildDataClosure(doneNodes);
					for (EGroumEdge e1 : e.source.inEdges) {
						if (e1 instanceof EGroumDataEdge) {
							EGroumDataEdge.Type type = ((EGroumDataEdge) e1).type;
							if ((e1.source.getAstNodeType() == ASTNode.METHOD_INVOCATION || e1.source.isCoreAction()) && (type == PARAMETER || type == RECEIVER))
								if (!this.hasInEdge(e1))
									new EGroumDataEdge(e1.source, this, de.type, de.label, true);
						}
					}
					continue;
				}
				if (!(e.source instanceof EGroumDataNode))
					continue;
				if (this instanceof EGroumActionNode && de.type != RECEIVER && de.type != PARAMETER && de.type != CONDITION)
					continue;
				if (this instanceof EGroumDataNode && de.type != QUALIFIER)
					continue;
				for (EGroumNode def : de.source.getDefinitions()) {
					for (EGroumEdge e1 : def.inEdges) {
						if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == DEFINITION) {
							if (!doneNodes.contains(e1.source))
								e1.source.buildDataClosure(doneNodes);
							for (EGroumEdge e2 : e1.source.inEdges) {
								if (e2 instanceof EGroumDataEdge) {
									EGroumDataEdge.Type type = ((EGroumDataEdge) e2).type;
									if (!e2.source.isLiteral() && (type == PARAMETER || type == RECEIVER) && !this.hasInEdge(e2))
										new EGroumDataEdge(e2.source, this, de.type, de.label, true);
								}
							}
							break;
						}
					}
				}
			}
		}
		doneNodes.add(this);
	}

	public void buildPreSequentialNodes(HashSet<EGroumNode> visitedNodes, HashMap<EGroumNode, HashSet<EGroumNode>> preNodesOfNode) {
		visitedNodes.add(this);
		HashSet<EGroumNode> preNodes = new HashSet<>();
		for (EGroumEdge e : this.inEdges) {
			preNodes.add(e.source);
			if (!visitedNodes.contains(e.source))
				e.source.buildPreSequentialNodes(visitedNodes, preNodesOfNode);
			preNodes.addAll(preNodesOfNode.get(e.source));
		}
		preNodesOfNode.put(this, preNodes);
	}

	public static EGroumNode createNode(EGroumNode node) {
		if (node instanceof EGroumActionNode)
			return new EGroumActionNode((EGroumActionNode) node);
		if (node instanceof EGroumControlNode)
			return new EGroumControlNode((EGroumControlNode) node);
		if (node instanceof EGroumDataNode)
			return new EGroumDataNode((EGroumDataNode) node);
		return null;
	}

	public static boolean isThisMethodCall(String label) {
		return label.startsWith("this.") && label.endsWith("()");
	}

	public void consumeDefStore(HashMap<String, HashSet<EGroumDataNode>> otherDefStore) {
		HashMap<String, HashSet<EGroumDataNode>> temp = new HashMap<>();
		for (String key : otherDefStore.keySet())
			if (!this.defStore.containsKey(key))
				temp.put(key, new HashSet<>(otherDefStore.get(key)));
		for (String key : temp.keySet()) {
			HashSet<EGroumDataNode> otherDefs = temp.get(key), defs = this.defStore.get(key);
			if (defs == null) {
				defs = new HashSet<>();
				this.defStore.put(key, defs);
			}
			defs.addAll(otherDefs);
			otherDefs.clear();
		}
		temp.clear();
	}

	public void consumeDefStore(HashSet<EGroumNode> sinks) {
		HashMap<String, HashSet<EGroumDataNode>> defStores = new HashMap<>();
		for (EGroumNode sink : sinks)
			update(defStores, sink.defStore);
		consumeDefStore(defStores);
	}

	public void consumeDefStore(EGroumGraph g) {
		consumeDefStore(g.sinks);
	}

	private void update(HashMap<String, HashSet<EGroumDataNode>> defStores, HashMap<String, HashSet<EGroumDataNode>> otherDefStore) {
		for (String key : otherDefStore.keySet()) {
			HashSet<EGroumDataNode> defs = defStores.get(key);
			if (defs == null) {
				defs = new HashSet<>();
				defStores.put(key, defs);
			}
			defs.addAll(otherDefStore.get(key));
		}
	}
}
