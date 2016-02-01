/**
 * 
 */
package groum;

import java.util.*;

import org.eclipse.jdt.core.dom.*;


/**
 * @author hoan
 *
 */
public class ASTFieldVisitor extends ASTVisitor{
	private HashMap<String, String> fieldType = new HashMap<String, String>();
	private ArrayList<String> fields = new ArrayList<String>();
	String className = "";
	private HashMap<ASTNode,ArrayList<ASTNode>> mapTree = new HashMap<ASTNode,ArrayList<ASTNode>>();
	private HashMap<String, String> imports = new HashMap<String, String>();
	
	public HashMap<String, String> getFieldType() {
		return fieldType;
	}

	public ArrayList<String> getFields() {
		return fields;
	}

	public String getClassName() {
		return className;
	}

	public HashMap<ASTNode, ArrayList<ASTNode>> getMapTree() {
		return mapTree;
	}
	
	public HashMap<String, String> getImports() {
		return imports;
	}

	@Override
	public void preVisit(ASTNode node) {
		 mapTree.put(node, new ArrayList<ASTNode>());
		 
		 //Add child node to its parent
		 if (node.getParent() != null){
			 ArrayList<ASTNode> children = mapTree.get(node.getParent());
			 children.add(node);
			 mapTree.put(node.getParent(), children);
		 }
			 
	}
	
	@Override
	public boolean visit(ImportDeclaration node)
	{
		if(!node.toString().contains("*"))
			imports.put(utils.FileIO.getSimpleClassName(node.getName().getFullyQualifiedName()), node.getName().getFullyQualifiedName());
		
		return true;
	}
	@Override
	public boolean visit(TypeDeclaration node)
    {
    	if(node.getParent().getNodeType() == ASTNode.COMPILATION_UNIT)
    	{
    		className = node.getName().toString();
    		fieldType.put("this", className);
    		fields.add("this");
    		if(node.getSuperclassType() != null)
    		{
    			String superClass = utils.FileIO.getSimpleClassName(node.getSuperclassType().toString());
   				fieldType.put("super", superClass);
   				fields.add("super");
    		}
    	}
    	
    	return true;
    }
	
	@Override
	public boolean visit(FieldDeclaration fieldDec){
		List<VariableDeclarationFragment> varDecFrag = fieldDec.fragments();
		for (VariableDeclarationFragment fragment : varDecFrag) {
			// System.out.print(fragment.getName()+",");
			String variable = fragment.getName().toString();
			String type = utils.FileIO.getSimpleClassName(fieldDec.getType().toString());
			fieldType.put(variable, type);
			fields.add(variable);
		}

		return true;
	}
}
