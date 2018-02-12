package edu.iastate.cs.egroum.aug;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;

import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.*;

public class EGroumControlNode extends EGroumNode {

	public EGroumControlNode(EGroumNode control, String branch, ASTNode astNode, int nodeType) {
		super(astNode, nodeType);
		this.control = control;
		new EGroumControlEdge(control, this, branch);
	}

	public EGroumControlNode(EGroumControlNode node) {
		super(node.astNode, node.astNodeType);
		if (node.control != null)
			this.control = node.control;
	}

	@Override
	public String getLabel() {
		return ASTNode.nodeClassForType(astNodeType).getSimpleName();
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	@Override
	public Optional<String> getAPI() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return getLabel();
	}
	
	@Override
	public boolean isSame(EGroumNode node) {
		if (node instanceof EGroumControlNode)
			return astNodeType == node.astNodeType;
		return false;
	}
	
	@Override
	public void buildControlClosure(HashSet<EGroumNode> doneNodes) {
		for (EGroumEdge e : new HashSet<EGroumEdge>(this.inEdges)) {
			if (e instanceof EGroumControlEdge) {
				if (!doneNodes.contains(e.source))
					e.source.buildControlClosure(doneNodes);
				for (EGroumEdge e1 : e.source.inEdges) {
					if (!this.hasInEdge(e1)) {
						if (e1 instanceof EGroumControlEdge)
							new EGroumControlEdge(e1.source, this, ((EGroumControlEdge) e1).label);
						else
							new EGroumDataEdge(e1.source, this, ((EGroumDataEdge) e1).type, ((EGroumDataEdge) e1).label);
					}
				}
			}
		}
		doneNodes.add(this);
	}

	protected void buildConditionClosure() {
		HashSet<EGroumNode> conditionNodes = new HashSet<>();
		for (EGroumEdge edge : inEdges) {
			if (!(edge instanceof EGroumDataEdge) || ((EGroumDataEdge) edge).type != CONDITION)
				continue;
			LinkedList<EGroumNode> nodes = new LinkedList<>();
			nodes.add(edge.source);
			while (!nodes.isEmpty()) {
				EGroumNode node = nodes.removeFirst();
				conditionNodes.add(node);
				for (EGroumEdge e : node.inEdges) {
					if (e instanceof EGroumDataEdge && !conditionNodes.contains(e.source)) {
						EGroumDataEdge de = (EGroumDataEdge) e;
						if (de.type == PARAMETER || de.type == QUALIFIER || de.type == RECEIVER)
							nodes.add(de.source);
					}
				}
			}
		}
		for (EGroumNode node : conditionNodes)
			if (node instanceof EGroumActionNode && !this.hasInNode(node))
				new EGroumDataEdge(node, this, CONDITION, getConditionLabel());
	}

	public boolean controlsAnotherNode(EGroumNode node) {
		EGroumEdge e = this.outEdges.get(getOutEdgeIndex(node));
		for (EGroumEdge oe : this.outEdges) {
			if (oe.getLabel().equals(e.getLabel()) && !oe.target.getLabel().equals(node.getLabel()))
				return true;
		}
		
		return false;
	}
}
