package edu.iastate.cs.egroum.aug;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Optional;

public class EGroumEntryNode extends EGroumNode {
	private String label;
	
	public EGroumEntryNode(ASTNode astNode, int nodeType, String label) {
		super(astNode, nodeType);
		this.label = label;
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
	public Optional<String> getAPI() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
