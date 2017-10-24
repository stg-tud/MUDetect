package egroum;

import org.eclipse.jdt.core.dom.ASTNode;

public class EGroumConstantNode extends EGroumDataNode {
	protected String dataValue;
	private int encodeLevel = 0;

	public EGroumConstantNode(ASTNode astNode, int nodeType, String key, String dataType, String dataName, String dataValue, boolean isField, boolean isDeclaration, int encodeLevel) {
		super(astNode, nodeType, key, dataType, dataName, isField, isDeclaration);
		this.dataValue = dataValue;
		this.encodeLevel = encodeLevel;
	}

	public String getDataValue() {
		return dataValue;
	}

	@Override
	public String getLabel() {
		if (encodeLevel >= 2 && dataValue != null)
			return dataValue;
		if (encodeLevel >= 1 && dataName != null)
			return dataName;
		return dataType;
	}

}
