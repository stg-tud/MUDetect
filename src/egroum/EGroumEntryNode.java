package egroum;

import org.eclipse.jdt.core.dom.ASTNode;

public class EGroumEntryNode extends EGroumNode {
	private String label;
	
	public EGroumEntryNode(ASTNode astNode, int nodeType, String label) {
		super(astNode, nodeType);
		this.label = label;
	}

	@Override
	public String getAbstractLabel() {
		return label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	@Override
	public boolean isAPI() {
		return false;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
