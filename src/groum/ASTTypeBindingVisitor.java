package groum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ASTTypeBindingVisitor extends ASTVisitor {

	private HashMap<String, String> localType = new HashMap<String, String>();
	private HashMap<String, String> fieldType = new HashMap<String, String>();
	private Stack<HashSet<String>> scopedTypes = new Stack<HashSet<String>>();
	
	public ASTTypeBindingVisitor(HashMap<String, String> fieldType){
		this.fieldType = fieldType;
	}
	//for variable dec in parameter list
	@Override
	public boolean visit(SingleVariableDeclaration singVarDec){
		String variable = singVarDec.getName().toString();
		String type = utils.FileIO.getSimpleClassName(singVarDec.getType().toString());
		/*if(singVarDec.getType().isSimpleType())
		{
			if(imports.containsKey(type))
				type = imports.get(type);
			else
				type = packageName + "." + type;
		}*/
		localType.put(variable, type);
		HashSet<String> mine = scopedTypes.pop();
		HashSet<String> parents = scopedTypes.pop();
		parents.add(variable);
		scopedTypes.push(parents);
		scopedTypes.push(mine);
		return true;
	}
	//"for" express
	@Override
	public boolean visit(VariableDeclarationExpression varDecExp) {
		// System.out.print("BINDING:");
		List<VariableDeclarationFragment> varDecFrag = varDecExp.fragments();
		HashSet<String> mine = scopedTypes.pop();
		HashSet<String> parents = scopedTypes.pop();
		for (VariableDeclarationFragment fragment : varDecFrag) {
			// System.out.print(fragment.getName()+",");
			String variable = fragment.getName().toString();
			String type = utils.FileIO.getSimpleClassName(varDecExp.getType().toString());
			/*if(varDecExp.getType().isSimpleType())
			{
				if(imports.containsKey(type))
					type = imports.get(type);
				else
					type = packageName + "." + type;
			}*/
			localType.put(variable, type);
			parents.add(variable);
		}
		// System.out.println("with type:" + sds.getType());
		scopedTypes.push(parents);
		scopedTypes.push(mine);

		return true;
	}
	@Override
	public boolean visit(VariableDeclarationStatement varDecStat) {
		// System.out.print("BINDING:");
		List<VariableDeclarationFragment> varDecFrag = varDecStat.fragments();
		HashSet<String> mine = scopedTypes.pop();
		HashSet<String> parents = scopedTypes.pop();
		for (VariableDeclarationFragment fragment : varDecFrag) {
			String variable = fragment.getName().toString();
			String type = utils.FileIO.getSimpleClassName(varDecStat.getType().toString());
			/*if(varDecStat.getType().isSimpleType())
			{
				if(imports.containsKey(type))
					type = imports.get(type);
				else
					type = packageName + "." + type;
			}*/
			localType.put(variable, type);
			parents.add(variable);
			// System.out.print(fragment.getName()+",");
		}
		// System.out.println("with type:" + varDecExp.getType());
		scopedTypes.push(parents);
		scopedTypes.push(mine);

		return true;
	}
	@Override
	public void preVisit(ASTNode node) {
		/*for(int i = 0; i < indent; i++)
    		System.out.print("\t");
    	String strNodeType = node.getClass().getSimpleName();
    	System.out.println(strNodeType + " { " + ((node.getNodeType() == ASTNode.SIMPLE_NAME) ? node.toString() : ""));
    	indent++;*/
    	
		scopedTypes.push(new HashSet<String>());
	}
	@Override
	public void postVisit(ASTNode node) 
	{
		HashSet<String> types = scopedTypes.pop();
		if(types != null && !types.isEmpty())
			for(String type : types)
			{
				localType.remove(type);
			}
		
		/*indent--;
    	for(int i = 0; i < indent; i++)
    		System.out.print("\t");
    	System.out.println("}" + (scopedMethods.isEmpty() ? "" : scopedMethods.peek()));*/
	}
	@Override
	public boolean visit(MethodInvocation node)
	{
		String objectName = (node.getExpression() != null) ? node.getExpression().toString() : "this";

		if(objectName.equals("this"))
		{
			node.setProperty("TypeBinding", fieldType.get(objectName));
		}
		else if (localType.containsKey(objectName)){
			node.setProperty("TypeBinding", localType.get(objectName));
		}
		else if (fieldType.containsKey(objectName)){
			node.setProperty("TypeBinding", fieldType.get(objectName));
		}
		else if(node.getExpression().getNodeType() == ASTNode.SIMPLE_NAME && Character.isUpperCase(objectName.charAt(0)))
		{
			node.setProperty("TypeBinding", objectName);
		}
		else
		{
			String name = node.getName().toString();
			if(node.getExpression().getNodeType() == ASTNode.CAST_EXPRESSION)
			{
				CastExpression expr = (CastExpression)(node.getExpression());
				objectName = utils.FileIO.getSimpleClassName(expr.getType().toString());
				node.setProperty("TypeBinding", objectName);
			}
			else if(node.getExpression().getNodeType() == ASTNode.METHOD_INVOCATION)
			{
				MethodInvocation expr = (MethodInvocation)(node.getExpression());
				objectName = expr.getName().toString();
				node.setProperty("TypeBinding", objectName);
			}
			else if(node.getExpression().getNodeType() == ASTNode.STRING_LITERAL)
			{
				node.setProperty("TypeBinding", "String");
			}
			
			else if(node.getExpression().getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION)
			{
				ParenthesizedExpression parenthsis = (ParenthesizedExpression)(node.getExpression());
				objectName = parenthsis.getExpression().toString();
				if (localType.containsKey(objectName)){
					node.setProperty("TypeBinding", localType.get(objectName));
				}
				else if (fieldType.containsKey(objectName)){
					node.setProperty("TypeBinding", fieldType.get(objectName));
				}
				else if(parenthsis.getExpression().getNodeType() == ASTNode.CAST_EXPRESSION)
				{
					CastExpression expr = (CastExpression)(parenthsis.getExpression());
					objectName = utils.FileIO.getSimpleClassName(expr.getType().toString());
					node.setProperty("TypeBinding", objectName);
				}
				else if(parenthsis.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION)
				{
					ClassInstanceCreation expr = (ClassInstanceCreation)(parenthsis.getExpression());
					objectName = utils.FileIO.getSimpleClassName(expr.getType().toString());
					node.setProperty("TypeBinding", objectName);
				}
				else if(parenthsis.getExpression().getNodeType() == ASTNode.METHOD_INVOCATION)
				{
					MethodInvocation expr = (MethodInvocation)(parenthsis.getExpression());
					objectName = expr.getName().toString();
					node.setProperty("TypeBinding", objectName);
				}
				else
				{
					//System.err.println(parenthsis);
					objectName = parenthsis.getExpression().toString();
					if(name.equals("equals") || name.equals("toString") || name.equals("getName") || name.equals("write") || name.equals("writeln"))
					{
						objectName = objectName.substring(objectName.lastIndexOf('.') + 1);
						node.setProperty("TypeBinding", objectName);
					}
					else
					{
						node.setProperty("TypeBinding", "#Uknown#");
					}
				}
			}
			else if(node.getExpression().getNodeType() == ASTNode.SIMPLE_NAME && Character.isUpperCase(objectName.charAt(0)))
			{
				node.setProperty("TypeBinding", objectName);
			}
			else if(name.equals("equals") || name.equals("toString") || name.equals("getName") || name.equals("write") || name.equals("writeln"))
			{
				//System.err.println(node);
				objectName = objectName.substring(objectName.indexOf('.') + 1);
				node.setProperty("TypeBinding", objectName);
			}
			else
			{
				//System.err.println(node);
				node.setProperty("TypeBinding", "#Uknown#");
			}
		}
		
		return true;
	}
	@Override
	public boolean visit(SimpleName node)
	{
		int pType = node.getParent().getNodeType();
		if(pType == ASTNode.QUALIFIED_NAME)
		{
			QualifiedName parent = (QualifiedName)node.getParent();
			String name = parent.getQualifier().toString();
			if(parent.getParent().getNodeType() != ASTNode.QUALIFIED_NAME && name.indexOf('.') == -1)
			{
				if (localType.containsKey(name)){
					node.setProperty("TypeBinding", localType.get(name) + "." + name);
				}
				else if (fieldType.containsKey(name)){
					node.setProperty("TypeBinding", fieldType.get(name) + "." + name);
				}
				else if(Character.isUpperCase(name.charAt(0)))
					node.setProperty("TypeBinding", name + "." + name);
			}
		}
		else if(pType == ASTNode.FIELD_ACCESS)
		{
			
		}
		else if(pType == ASTNode.ASSIGNMENT)
		{
			
		}
		else if(pType == ASTNode.METHOD_INVOCATION)
		{
			
		}
		else
		{
			String name = node.toString();
			if (!localType.containsKey(name) && fieldType.containsKey(name)){
				node.setProperty("TypeBinding", fieldType.get("this") + ".this");
			}
		}
		
		return true;
	}
	@Override
	public boolean visit(FieldAccess node)
	{
		String objectName = node.getExpression().toString();
		if(objectName.equals("this"))
		{
			node.setProperty("TypeBinding", fieldType.get("this"));
		}
		else if (localType.containsKey(objectName)){
			node.setProperty("TypeBinding", localType.get(objectName));
		}
		else if (fieldType.containsKey(objectName)){
			node.setProperty("TypeBinding", fieldType.get("this"));
		}
		else
		{
			String typeName = objectName;
			if(objectName.contains("."))
			{
				String rootName = objectName.substring(0, objectName.indexOf("."));
				String remain = objectName.substring(objectName.indexOf("."));
				if(rootName.equals("this"))
				{
					typeName = fieldType.get("this") + remain;
				}
				else if (localType.containsKey(rootName)){
					typeName = localType.get(rootName) + remain;
				}
				else if (fieldType.containsKey(rootName)){
					typeName = fieldType.get("this") + remain;	
				}
			}
			node.setProperty("TypeBinding", typeName);
		}
		
		return true;
	}
	@Override
	public boolean visit(ClassInstanceCreation node)
	{
		String typeName = utils.FileIO.getSimpleClassName(node.getType().toString());
		node.setProperty("TypeBinding", typeName);
		
		return true;
	}
	@Override
	public boolean visit(CastExpression node)
	{
		String type = utils.FileIO.getSimpleClassName(node.getType().toString());
		node.setProperty("TypeBinding", type);
		
		return true;
	}
	@Override
	public boolean visit(InstanceofExpression node)
	{
		String type = utils.FileIO.getSimpleClassName(node.getRightOperand().toString());
		node.setProperty("TypeBinding", type);
		
		return true;
	}
	@Override
	public boolean visit(SuperMethodInvocation node)
	{
		node.setProperty("TypeBinding", fieldType.get("super"));
		
		return true;
	}
	@Override
	public boolean visit(ConstructorInvocation node)
	{
		node.setProperty("TypeBinding", fieldType.get("this"));
		
		return true;
	}
	@Override
	public boolean visit(SuperConstructorInvocation node)
	{
		node.setProperty("TypeBinding", fieldType.get("super"));
		
		return true;
	}
	@Override
	public boolean visit(SuperFieldAccess node)
	{
		node.setProperty("TypeBinding", fieldType.get("super"));
		
		return true;
	}
	@Override
	public boolean visit(Assignment node)
	{
		
		return true;
	}
}
