package edu.iastate.cs.egroum.utils;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;

public class TypeResolveVisitor extends ASTVisitor {

	public static void main(String[] args) {
		File file = new File("src/main/java/de/tu_darmstadt/stg/mudetect/utils/TypeResolveVisitor.java");
		String name = file.getName();
		String source = FileIO.readStringFromFile(file.getAbsolutePath());
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
    	parser.setCompilerOptions(options);
		parser.setEnvironment(
				new String[]{"D:/Projects/aug-miner.jar"}, 
				new String[]{}, 
				new String[]{}, 
				true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setSource(source.toCharArray());
    	parser.setUnitName(name);
    	ASTNode ast = parser.createAST(null);
    	ast.accept(new TypeResolveVisitor());
	}
	
	@Override
	public void preVisit(ASTNode node) {
		ITypeBinding b = null;
		if (node instanceof Name) {
			Name name = (Name) node;
			b = name.resolveTypeBinding();
			if (b != null)
				System.out.println(node.toString() + ": " + b.getQualifiedName());
			else
				System.out.println(node.toString() + ": null");
		} else if (node instanceof Expression) {
			Expression e = (Expression) node;
			b = e.resolveTypeBinding();
			if (b != null)
				System.out.println(node.toString() + ": " + b.getQualifiedName());
			else
				System.out.println(node.toString() + ": null");
		}
	}
}
