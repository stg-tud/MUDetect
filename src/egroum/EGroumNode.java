package egroum;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

import egroum.EGroumDataEdge.Type;
import utils.JavaASTUtil;

public abstract class EGroumNode {
	protected static final String PREFIX_DUMMY = "dummy_";
	
	protected ASTNode astNode;
	protected int astNodeType;
	protected String key;
	protected EGroumNode control;
	protected String dataType;
	protected ArrayList<EGroumEdge> inEdges = new ArrayList<EGroumEdge>();
	protected ArrayList<EGroumEdge> outEdges = new ArrayList<EGroumEdge>();

	public int version;
	
	public EGroumNode(ASTNode astNode, int nodeType) {
		this.astNode = astNode;
		this.astNodeType = nodeType;
	}
	
	public EGroumNode(ASTNode astNode, int nodeType, String key) {
		this(astNode, nodeType);
		this.key = key;
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
	
	abstract public String getExasLabel();

	public int getAstNodeType() {
		return astNodeType;
	}

	public ASTNode getAstNode() {
		return astNode;
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

	public boolean isStatement() {
		return control != null;
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
			if (!(e instanceof EGroumDataEdge) || ((EGroumDataEdge) e).type != Type.DEPENDENCE)
				edges.add(e);
		return edges;
	}
	
	ArrayList<EGroumEdge> getOutEdgesForExasVectorization() {
		ArrayList<EGroumEdge> edges = new ArrayList<>();
		for (EGroumEdge e : outEdges)
			if (!(e instanceof EGroumDataEdge) || ((EGroumDataEdge) e).type != Type.DEPENDENCE)
				edges.add(e);
		return edges;
	}

	public boolean isEmptyNode() {
		return this instanceof EGroumActionNode && ((EGroumActionNode) this).name.equals("empty");
	}

	private void adjustControl(EGroumNode empty, int index) {
		EGroumControlEdge e = (EGroumControlEdge) getInEdge(control);
		control.outEdges.remove(e);
		e.source = empty.control;
		empty.control.outEdges.add(index, e);
		e.label = empty.getInEdge(empty.control).getLabel();
		control = empty.control;
	}

	public EGroumEdge getInEdge(EGroumNode node) {
		for (EGroumEdge e : inEdges)
			if (e.source == node)
				return e;
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
			if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.DEPENDENCE)
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

	public void addNeighbors(HashSet<EGroumNode> nodes) {
		for (EGroumEdge e : inEdges)
			if (!(e instanceof EGroumDataEdge) || (((EGroumDataEdge) e).type != Type.DEPENDENCE && ((EGroumDataEdge) e).type != Type.REFERENCE)) {
				if (!e.source.isEmptyNode() && !nodes.contains(e.source)) {
					nodes.add(e.source);
					e.source.addNeighbors(nodes);
				}
			}
		for (EGroumEdge e : outEdges)
			if (!(e instanceof EGroumDataEdge) || (((EGroumDataEdge) e).type != Type.DEPENDENCE && ((EGroumDataEdge) e).type != Type.REFERENCE)) {
				if (!e.target.isEmptyNode() && !nodes.contains(e.target)) {
					nodes.add(e.target);
					e.target.addNeighbors(nodes);
				}
			}
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

	public EGroumNode getDefinition() {
		if (this instanceof EGroumDataNode && this.inEdges.size() == 1 && this.inEdges.get(0) instanceof EGroumDataEdge) {
			EGroumDataEdge e = (EGroumDataEdge) this.inEdges.get(0);
			if (e.type == Type.REFERENCE)
				return e.source;
		}
		return null;
	}

	public ArrayList<EGroumNode> getDefinitions() {
		ArrayList<EGroumNode> defs = new ArrayList<>();
		if (this instanceof EGroumDataNode) {
			for (EGroumEdge e : this.inEdges) {
				if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.REFERENCE)
					defs.add(e.source);
			}
		}
		return defs;
	}

	public boolean hasInEdge(EGroumNode node, String label) {
		for (EGroumEdge e : inEdges)
			if (e.source == node && e.getLabel().equals(label))
				return true;
		return false;
	}

	public boolean hasInEdge(EGroumEdge edge) {
		for (EGroumEdge e : inEdges)
			if (e.source == edge.source && e.getLabel().equals(edge.getLabel()))
				return true;
		return false;
	}

	public boolean hasInNode(EGroumNode preNode) {
		for (EGroumEdge e : inEdges)
			if (e.source == preNode)
				return true;
		return false;
	}

	public boolean hasOutNode(EGroumNode target) {
		for (EGroumEdge e : outEdges)
			if (e.target == target)
				return true;
		return false;
	}

	public boolean isValid() {
		HashSet<EGroumNode> s = new HashSet<>();
		for (EGroumEdge e : outEdges) {
			if(e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.DEPENDENCE)
				continue;
			if (s.contains(e.target))
				return false;
			s.add(e.target);
		}
		return true;
	}
}
