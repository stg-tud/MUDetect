package egroum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import utils.JavaASTUtil;

public class EGroumBuildingContext {
	public static HashMap<String, HashMap<String, String>> typeFieldTypes = new HashMap<>();
	
	private MethodDeclaration method;
	protected boolean interprocedural;
	private Stack<HashSet<EGroumActionNode>> stkTrys = new Stack<>();
	private Stack<HashMap<String, String>> localVariables = new Stack<>(), localVariableTypes = new Stack<>();
	private HashMap<String, String> fieldTypes = new HashMap<>();
	
	public EGroumBuildingContext(boolean interprocedural) {
		this.interprocedural = interprocedural;
	}
	
	public EGroumBuildingContext(EGroumBuildingContext context) {
		this.interprocedural = context.interprocedural;
	}

	public void setMethod(MethodDeclaration method) {
		this.method = method;
		ASTNode p = this.method.getParent();
		if (p != null) {
			if (p instanceof TypeDeclaration)
				buildFieldTypes((TypeDeclaration) p);
			else if (p instanceof EnumDeclaration)
				buildFieldTypes((EnumDeclaration) p);
		}
	}
	
	private void buildFieldTypes(EnumDeclaration ed) {
		for (int i = 0; i < ed.bodyDeclarations().size(); i++) {
			if (ed.bodyDeclarations().get(i) instanceof FieldDeclaration) {
				FieldDeclaration f = (FieldDeclaration) ed.bodyDeclarations().get(i);
				String type = JavaASTUtil.getSimpleType(f.getType());
				for (int j = 0; j < f.fragments().size(); j++) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) f.fragments().get(j);
					this.fieldTypes.put(vdf.getName().getIdentifier(), type);
				}
			}
		}
		ASTNode p = ed.getParent();
		if (p != null) {
			if (p instanceof TypeDeclaration)
				buildFieldTypes((TypeDeclaration) p);
			else if (p instanceof EnumDeclaration)
				buildFieldTypes((EnumDeclaration) p);
		}
	}

	private void buildFieldTypes(TypeDeclaration td) {
		for (FieldDeclaration f : td.getFields()) {
			String type = JavaASTUtil.getSimpleType(f.getType());
			for (int i = 0; i < f.fragments().size(); i++) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) f.fragments().get(i);
				this.fieldTypes.put(vdf.getName().getIdentifier(), type);
			}
		}
		ASTNode p = td.getParent();
		if (p != null) {
			if (p instanceof TypeDeclaration)
				buildFieldTypes((TypeDeclaration) p);
			else if (p instanceof EnumDeclaration)
				buildFieldTypes((EnumDeclaration) p);
		}
	}

	public void addMethodTry(EGroumActionNode node) {
		for (int i = 0; i < stkTrys.size(); i++)
			stkTrys.get(i).add(node);
	}

	public void pushTry() {
		stkTrys.push(new HashSet<EGroumActionNode>());
	}
	
	public HashSet<EGroumActionNode> popTry() {
		return stkTrys.pop();
	}

	public HashSet<EGroumActionNode> getTrys(ITypeBinding catchExceptionType) {
		HashSet<EGroumActionNode> trys = new HashSet<>();
		for (EGroumActionNode node : stkTrys.peek())
			if (node.exceptionTypes != null) {
				for (ITypeBinding type : node.exceptionTypes)
					if (isSubType(type, catchExceptionType)) {
						trys.add(node);
						break;
					}
			}
		return trys;
	}

	private boolean isSubType(ITypeBinding type, ITypeBinding catchExceptionType) {
		if (type == null) 
			return false;
		if (type.equals(catchExceptionType))
			return true;
		return isSubType(type.getSuperclass(), catchExceptionType);
	}

	public void addMethodTrys(HashSet<EGroumActionNode> nodes) {
		for (int i = 0; i < stkTrys.size(); i++)
			stkTrys.get(i).addAll(nodes);
	}

	public String getKey(ArrayAccess astNode) {
		String name = null;
		Expression a = astNode.getArray();
		if (a instanceof ArrayAccess)
			name = getKey((ArrayAccess) astNode.getArray());
		else if (a instanceof FieldAccess) {
			name = a.toString();
		} 
		else if (a instanceof QualifiedName)
			name = ((QualifiedName) a).getFullyQualifiedName();
		else if (a instanceof SimpleName) {
			name = ((SimpleName) a).getIdentifier();
			String[] info = getLocalVariableInfo(name);
			if (info != null)
				name = info[0];
		} else if (a instanceof SuperFieldAccess) {
			name = a.toString();
		}
		if (astNode.getIndex() instanceof NumberLiteral)
			name += "[" + ((NumberLiteral) (astNode.getIndex())).getToken()
					+ "]";
		else
			name += "[int]";
		return name;
	}

	public String[] getLocalVariableInfo(String identifier) {
		for (int i = localVariables.size() - 1; i >= 0; i--) {
			HashMap<String, String> variables = this.localVariables.get(i);
			if (variables.containsKey(identifier))
				return new String[]{variables.get(identifier), this.localVariableTypes.get(i).get(identifier)};
		}
		return null;
	}

	public void addScope() {
		this.localVariables.push(new HashMap<String, String>());
		this.localVariableTypes.push(new HashMap<String, String>());
	}

	public void removeScope() {
		this.localVariables.pop();
		this.localVariableTypes.pop();
	}

	public void addLocalVariable(String identifier, String key, String type) {
		this.localVariables.peek().put(identifier, key);
		this.localVariableTypes.peek().put(identifier, type);
	}

	public String getFieldType(String name) {
		String type = this.fieldTypes.get(name);
		if (type == null) {
			buildSuperFieldTypes();
			type = this.fieldTypes.get(name);
		}
		return type;
	}

	public void buildSuperFieldTypes() {
		ASTNode p = this.method.getParent();
		if (p != null && p instanceof TypeDeclaration)
			buildSuperFieldTypes((TypeDeclaration) p);
	}

	private void buildSuperFieldTypes(TypeDeclaration td) {
		if (td.getSuperclassType() != null) {
			String stype = JavaASTUtil.getSimpleType(td.getSuperclassType());
			buildSuperFieldTypes(stype);
		}
		ASTNode p = td.getParent();
		if (p != null && p instanceof TypeDeclaration)
			buildSuperFieldTypes((TypeDeclaration) p);
	}

	private void buildSuperFieldTypes(String stype) {
		//TODO
	}
}
