package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

public class JavaASTUtil {

	public static ASTNode parseSource(String source) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
    	parser.setSource(source.toCharArray());
    	parser.setCompilerOptions(options);
    	ASTNode ast = parser.createAST(null);
		return ast;
	}
	
	public static ASTNode parseSource(String source, int kind) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
    	parser.setSource(source.toCharArray());
    	parser.setCompilerOptions(options);
    	parser.setKind(kind);
    	ASTNode ast = parser.createAST(null);
		return ast;
	}
	
	public static String getSource(ASTNode node) {
		NaiveASTFlattener flatterner = new NaiveASTFlattener();
		node.accept(flatterner);
		return flatterner.getResult();
	}

	public static boolean isLiteral(int astNodeType) {
		return ASTNode.nodeClassForType(astNodeType).getSimpleName().endsWith("Literal");
	}

	public static boolean isLiteral(ASTNode node) {
		int type = node.getNodeType();
		if (type == ASTNode.BOOLEAN_LITERAL || 
				type == ASTNode.CHARACTER_LITERAL || 
				type == ASTNode.NULL_LITERAL || 
				type == ASTNode.NUMBER_LITERAL || 
				type == ASTNode.STRING_LITERAL)
			return true;
		if (type == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression pe = (PrefixExpression) node;
			return isLiteral(pe.getOperand());
		}
		if (type == ASTNode.POSTFIX_EXPRESSION) {
			PostfixExpression pe = (PostfixExpression) node;
			return isLiteral(pe.getOperand());
		}
		if (type == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression pe = (ParenthesizedExpression) node;
			return isLiteral(pe.getExpression());
		}
		
		return false;
	}

	public static boolean isPublic(MethodDeclaration declaration) {
		for (int i = 0; i < declaration.modifiers().size(); i++) {
			Modifier m = (Modifier) declaration.modifiers().get(i);
			if (m.isPublic())
				return true;
		}
		return false;
	}

	public static String buildSignature(MethodDeclaration method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName().getIdentifier() + "#");
		for (int i = 0; i < method.parameters().size(); i++) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) method.parameters().get(i);
			sb.append(JavaASTUtil.getSimpleType(svd.getType()));
		}
		return sb.toString();
	}

	public static String getSimpleType(VariableDeclarationFragment f) {
		ASTNode p = f.getParent();
		if (p instanceof FieldDeclaration)
			return getSimpleType(((FieldDeclaration) p).getType());
		if (p instanceof VariableDeclarationStatement)
			return getSimpleType(((VariableDeclarationStatement) p).getType());
		if (p instanceof VariableDeclarationExpression)
			return getSimpleType(((VariableDeclarationExpression) p).getType());
		throw new UnsupportedOperationException("Get type of a declaration!!!");
	}

	public static String getCompactType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getCompactType(t.getComponentType()) + "[]";
		}
		else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getCompactType(t.getType());
		}
		else if (type.isPrimitiveType()) {
			String pt = type.toString();
			if (pt.equals("byte") || pt.equals("short") || pt.equals("int") || pt.equals("long") 
					|| pt.equals("float") || pt.equals("double"))
				return "number";
			return pt;
		}
		else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return getCompactType(t.getQualifier()) + "." + t.getName().getIdentifier();
		}
		else if (type.isSimpleType()) {
			String pt = type.toString();
			if (pt.equals("Byte") || pt.equals("Short") || pt.equals("Integer") || pt.equals("Long") 
					|| pt.equals("Float") || pt.equals("Double"))
				return "number";
			return pt;
		}
		else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getSimpleType(t.getComponentType()) + "[]";
			//return type.toString();
		}
		else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType());
		}
		else if (type.isPrimitiveType()) {
			String pt = type.toString();
			if (pt.equals("byte") || pt.equals("short") || pt.equals("int") || pt.equals("long") 
					|| pt.equals("float") || pt.equals("double"))
				return "number";
			return pt;
		}
		else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		}
		else if (type.isSimpleType()) {
			String pt = type.toString();
			if (pt.equals("Byte") || pt.equals("Short") || pt.equals("Integer") || pt.equals("Long") 
					|| pt.equals("Float") || pt.equals("Double"))
				return "number";
			return pt;
		}
		else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getQualifiedType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getQualifiedType(t.getComponentType()) + "[]";
			//return type.toString();
		}
		else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getQualifiedType(t.getType());
		}
		else if (type.isPrimitiveType()) {
			return type.toString();
		}
		else if (type.isQualifiedType()) {
			return type.toString();
		}
		else if (type.isSimpleType()) {
			return type.toString();
		}
		else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleType(Type type, HashSet<String> typeParameters) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getSimpleType(t.getComponentType(), typeParameters) + "[]";
			//return type.toString();
		}
		else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType(), typeParameters);
		}
		else if (type.isPrimitiveType()) {
			return type.toString();
		}
		else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		}
		else if (type.isSimpleType()) {
			if (typeParameters.contains(type.toString()))
				return "Object";
			return type.toString();
		}
		else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleName(Name name) {
		if (name.isSimpleName())
			return name.toString();
		QualifiedName qn = (QualifiedName) name;
		return qn.getName().getIdentifier();
	}

	public static String getInfixOperator(Operator operator) {
		if (operator == Operator.ASSIGN)
			return null;
		String op = operator.toString();
		return op.substring(0, op.length() - 1);
	}
	
	public static TypeDeclaration getType(TypeDeclaration td, String name) {
		for (TypeDeclaration inner : td.getTypes())
			if (inner.getName().getIdentifier().equals(name))
				return inner;
		return null;
	}

	public static boolean isDeprecated(MethodDeclaration method) {
		Javadoc doc = method.getJavadoc();
		if (doc != null) {
			for (int i = 0; i < doc.tags().size(); i++) {
				TagElement tag = (TagElement) doc.tags().get(i);
				if (tag.getTagName() != null && tag.getTagName().toLowerCase().equals("@deprecated"))
					return true;
			}
		}
		return false;
	}

	public static int countLeaves(ASTNode node) {
		class LeaveCountASTVisitor extends ASTVisitor {
			private Stack<Integer> numOfChildren = new Stack<Integer>();
			private int numOfLeaves = 0;
			
			public LeaveCountASTVisitor() {
				numOfChildren.push(0);
			}
			
			@Override
			public void preVisit(ASTNode node) {
				int n = numOfChildren.pop();
				numOfChildren.push(n + 1);
				numOfChildren.push(0);
			}
			
			@Override
			public void postVisit(ASTNode node) {
				int n = numOfChildren.pop();
				if (n == 0)
					numOfLeaves++;
			}
		};
		LeaveCountASTVisitor v = new LeaveCountASTVisitor();
		node.accept(v);
		return v.numOfLeaves;
	}

	public static ArrayList<String> tokenizeNames(ASTNode node) {
		return new ASTVisitor() {
			private ArrayList<String> names = new ArrayList<>();
			
			@Override
			public boolean visit(org.eclipse.jdt.core.dom.SimpleName node) {
				names.add(node.getIdentifier());
				return false;
			};
		}.names;
	}
}
