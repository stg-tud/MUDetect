package egroum;

import org.eclipse.jdt.core.dom.ASTNode;

public class EGroumControlNode extends EGroumNode {

	public EGroumControlNode(EGroumNode control, String branch, ASTNode astNode, int nodeType) {
		super(astNode, nodeType);
		this.control = control;
		new EGroumControlEdge(control, this, branch);
	}

	@Override
	public String getLabel() {
		return ASTNode.nodeClassForType(astNodeType).getSimpleName();
	}

	@Override
	public String getExasLabel() {
		return ASTNode.nodeClassForType(astNodeType).getSimpleName();
	}
	
	@Override
	public boolean isDefinition() {
		return false;
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
}
