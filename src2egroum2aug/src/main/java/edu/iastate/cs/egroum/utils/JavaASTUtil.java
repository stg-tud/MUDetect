package edu.iastate.cs.egroum.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

public class JavaASTUtil {
	public static final HashMap<String, String> infixExpressionLables = new HashMap<>(), assignmentLabels = new HashMap<>();
	
	static {
		// Arithmetic Operators
		infixExpressionLables.put(InfixExpression.Operator.DIVIDE.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.MINUS.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.PLUS.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.REMAINDER.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.TIMES.toString(), "<a>");
		// Equality and Relational Operators
		infixExpressionLables.put(InfixExpression.Operator.EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.GREATER.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.GREATER_EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.LESS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.LESS_EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.NOT_EQUALS.toString(), "<r>");
		// Conditional Operators
		infixExpressionLables.put(InfixExpression.Operator.CONDITIONAL_AND.toString(), "<c>");
		infixExpressionLables.put(InfixExpression.Operator.CONDITIONAL_OR.toString(), "<c>");
		// Bitwise and Bit Shift Operators
		infixExpressionLables.put(InfixExpression.Operator.AND.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.OR.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.XOR.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.LEFT_SHIFT.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.toString(), "<b>");

		assignmentLabels.put(Assignment.Operator.ASSIGN.toString(), "=");
		// Arithmetic Operators
		assignmentLabels.put(Assignment.Operator.DIVIDE_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.MINUS_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.PLUS_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.REMAINDER_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.TIMES_ASSIGN.toString(), "<a>");
		// Bitwise and Bit Shift Operators
		assignmentLabels.put(Assignment.Operator.BIT_AND_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.BIT_OR_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.BIT_XOR_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.LEFT_SHIFT_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN.toString(), "<b>");
	}

	public static String getLabel(InfixExpression.Operator key) {
		return JavaASTUtil.infixExpressionLables.get(key.toString());
	}

	@SuppressWarnings("rawtypes")
	public static ASTNode parseSource(String source, String path, String name, String[] classpaths) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		String srcDir = getSrcDir(source, path, name);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
    	parser.setCompilerOptions(options);
		parser.setEnvironment(
				classpaths == null ? new String[]{} : classpaths, 
				new String[]{srcDir}, 
				new String[]{"UTF-8"}, 
				true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setSource(source.toCharArray());
    	parser.setUnitName(name);
		return parser.createAST(null);
	}

	@SuppressWarnings("rawtypes")
	public static ASTNode parseSource(String source) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
    	parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
    	ASTNode ast = parser.createAST(null);
		return ast;
	}
	
	@SuppressWarnings("rawtypes")
	private static String getSrcDir(String source, String path, String name) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
    	parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
    	ASTNode ast = parser.createAST(null);
    	CompilationUnit cu =  (CompilationUnit) ast;
    	String srcDir = path;
    	if (cu.getPackage() != null) {
    		String p = cu.getPackage().getName().getFullyQualifiedName();
    		int end = path.length() - p.length() - 1 - name.length();
    		if (end > 0)
    			srcDir = path.substring(0, end);
    	} else {
	    	int end = path.length() - name.length();
			if (end > 0)
				srcDir = path.substring(0, end);
    	}
		return srcDir;
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
			sb.append(JavaASTUtil.getSimpleType(svd.getType()) + "#");
		}
		return sb.toString();
	}
	
	public static String buildSignature(IMethodBinding mb) {
		StringBuilder sb = new StringBuilder();
		sb.append(mb.getName() + "#");
		for (ITypeBinding tb : mb.getParameterTypes())
			sb.append(tb.getTypeDeclaration().getName() + "#");
		return sb.toString();
	}

	public static String getSimpleType(VariableDeclarationFragment f) {
		String dimensions = "";
		for (int i = 0; i < f.getExtraDimensions(); i++)
			dimensions += "[]";
		ASTNode p = f.getParent();
		if (p instanceof FieldDeclaration)
			return getSimpleType(((FieldDeclaration) p).getType()) + dimensions;
		if (p instanceof VariableDeclarationStatement)
			return getSimpleType(((VariableDeclarationStatement) p).getType()) + dimensions;
		if (p instanceof VariableDeclarationExpression)
			return getSimpleType(((VariableDeclarationExpression) p).getType()) + dimensions;
		throw new UnsupportedOperationException("Get type of a declaration!!!");
	}

	public static String getSimpleType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			String pt = getSimpleType(t.getElementType());
			for (int i = 0; i < t.getDimensions(); i++)
				pt += "[]";
			return pt;
			//return type.toString();
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType());
		} else if (type.isPrimitiveType()) {
			String pt = type.toString();
			/*if (pt.equals("byte") || pt.equals("short") || pt.equals("int") || pt.equals("long") 
					|| pt.equals("float") || pt.equals("double"))
				return "number";*/
			return pt;
		} else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			SimpleType st = (SimpleType) type;
			String pt = st.getName().getFullyQualifiedName();
			if (st.getName() instanceof QualifiedName)
				pt = getSimpleName(st.getName());
			if (pt.isEmpty())
				pt = st.getName().getFullyQualifiedName();
			/*if (pt.equals("Byte") || pt.equals("Short") || pt.equals("Integer") || pt.equals("Long") 
					|| pt.equals("Float") || pt.equals("Double"))
				return "number";*/
			return pt;
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getSimpleType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += "&" + getSimpleType(types.get(i));
			return s;
		}  else if (type.isUnionType()) {
			UnionType ut = (UnionType) type;
			String s = getSimpleType((Type) ut.types().get(0));
			for (int i = 1; i < ut.types().size(); i++)
				s += "|" + getSimpleType((Type) ut.types().get(i));
			return s;
		} else if (type.isWildcardType()) {
			WildcardType t = (WildcardType) type;
			return getSimpleType(t.getBound());
		} else if (type.isNameQualifiedType()) {
			NameQualifiedType nqt = (NameQualifiedType) type;
			return nqt.getName().getIdentifier();
		} else if (type.isAnnotatable()) {
			return type.toString();
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleType(Type type, HashSet<String> typeParameters) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			String pt = getSimpleType(t.getElementType(), typeParameters);
			for (int i = 0; i < t.getDimensions(); i++)
				pt += "[]";
			return pt;
			//return type.toString();
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType(), typeParameters);
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			if (typeParameters.contains(type.toString()))
				return "Object";
			return type.toString();
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getSimpleType(types.get(0), typeParameters);
			for (int i = 1; i < types.size(); i++)
				s += "&" + getSimpleType(types.get(i), typeParameters);
			return s;
		} else if (type.isUnionType()) {
			UnionType ut = (UnionType) type;
			String s = getSimpleType((Type) ut.types().get(0), typeParameters);
			for (int i = 1; i < ut.types().size(); i++)
				s += "|" + getSimpleType((Type) ut.types().get(i), typeParameters);
			return s;
		} else if (type.isWildcardType()) {
			WildcardType t = (WildcardType) type;
			return getSimpleType(t.getBound(), typeParameters);
		} else if (type.isNameQualifiedType()) {
			NameQualifiedType nqt = (NameQualifiedType) type;
			return nqt.getName().getIdentifier();
		} else if (type.isAnnotatable()) {
			return type.toString();
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleName(Name name) {
		if (name.isSimpleName()) {
			SimpleName sn = (SimpleName) name;
			if (Character.isUpperCase(sn.getIdentifier().charAt(0)))
				return sn.getIdentifier();
			return "";
		}
		QualifiedName qn = (QualifiedName) name;
		if (Character.isUpperCase(qn.getFullyQualifiedName().charAt(0)))
			return qn.getFullyQualifiedName();
		String sqn = getSimpleName(qn.getQualifier());
		if (sqn.isEmpty())
			return getSimpleName(qn.getName());
		return sqn + "." + qn.getName().getIdentifier();
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

	public static String buildLabel(InfixExpression.Operator operator) {
		return infixExpressionLables.get(operator.toString());
	}

	public static String getAssignOperator(Operator operator) {
		return assignmentLabels.get(operator.toString());
	}

	public static boolean isConstant(IVariableBinding vb) {
		if (vb.isEnumConstant())
			return true;
		int modifiers = vb.getModifiers();
		return Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers);
	}
}
