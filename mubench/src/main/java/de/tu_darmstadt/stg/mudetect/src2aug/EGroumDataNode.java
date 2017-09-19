package de.tu_darmstadt.stg.mudetect.src2aug;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.src2aug.EGroumDataEdge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.src2aug.EGroumDataEdge.Type.QUALIFIER;
import static de.tu_darmstadt.stg.mudetect.src2aug.EGroumDataEdge.Type.THROW;

public class EGroumDataNode extends EGroumNode {
	protected boolean isField = false, isDeclaration = false;
	protected String dataName;
	protected String dataValue;
	protected int encodeLevel = 0;
	
	public EGroumDataNode(ASTNode astNode, int nodeType, String key, String dataType, String dataName) {
		super(astNode, nodeType, key);
		if (nodeType == ASTNode.METHOD_DECLARATION)
			this.dataType = dataType;
		else if (dataType.endsWith(")") || dataType.endsWith(">"))
			this.dataType = "UNKNOWN";
		else
			this.dataType = dataType;
		this.dataName = dataName;
	}
	
	public EGroumDataNode(ASTNode astNode, int nodeType, String key, String dataType, String dataName, boolean isField, boolean isDeclaration) {
		this(astNode, nodeType, key, dataType, dataName);
		this.isField = isField;
		this.isDeclaration = isDeclaration;
	}

	public EGroumDataNode(ASTNode astNode, int nodeType, String key, String dataType, String dataName, String dataValue, boolean isField, boolean isDeclaration, int encodeLevel) {
		this(astNode, nodeType, key, dataType, dataName, isField, isDeclaration);
		this.dataValue = dataValue;
		this.encodeLevel = encodeLevel;
	}
	
	public EGroumDataNode(EGroumDataNode node) {
		this(node.astNode, node.astNodeType, node.key, node.dataType, node.dataName, node.dataValue, node.isField, node.isDeclaration, node.encodeLevel);
	}

	public EGroumDataNode(String dataType) {
		super(null, ASTNode.SIMPLE_TYPE, null);
		this.dataType = dataType;
	}

	@Override
	public String getDataName() {
		return dataName;
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

	@Override
	public boolean isDefinition() {
		for (EGroumEdge e : inEdges)
			if (((EGroumDataEdge) e).type == DEFINITION)
				return true;
		return false;
	}

	public boolean isException() {
		for (EGroumEdge e : inEdges)
			if (((EGroumDataEdge) e).type == THROW)
				return true;
		return false;
	}
	
	@Override
	public boolean isSame(EGroumNode node) {
		if (node instanceof EGroumDataNode)
			return dataName.equals(((EGroumDataNode) node).dataName) && dataType.equals(((EGroumDataNode) node).dataType);
		return false;
	}

	public boolean isDummy() {
		return key.startsWith(PREFIX_DUMMY);
	}

	@Override
	public Optional<String> getAPI() {
		String label = getLabel();
		switch (label) {
			case "int":
			case "long":
			case "float":
			case "double":
			case "short":
			case "boolean":
				return Optional.empty();
			default:
				if (label.endsWith("[]")) {
					return Optional.empty();
				} else {
					return Optional.of(getLabel());
				}
		}
	}

	@Override
	public EGroumNode getQualifier() {
		for (EGroumEdge e : inEdges)
			if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == QUALIFIER)
				return e.source;
		return null;
	}

	public void copyData(EGroumDataNode node) {
		this.astNode = node.astNode;
		this.astNodeType = node.astNodeType;
		this.dataName = node.dataName;
		this.dataType = node.dataType;
		this.dataValue = node.dataValue;
		this.key = node.key;
		this.isField = node.isField;
		this.isDeclaration = node.isDeclaration;
		this.encodeLevel = node.encodeLevel;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
}
