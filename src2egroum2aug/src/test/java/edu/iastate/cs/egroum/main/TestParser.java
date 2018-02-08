package edu.iastate.cs.egroum.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.iastate.cs.egroum.utils.FileIO;

public class TestParser {
	private static ASTParser parser;
	private static HashMap<String, ASTNode> asts = new HashMap<>();
	private static int mismatches = 0;

	public static void main(String[] args) {
		ArrayList<File> files = getPaths(new String[] {"src"});
		parse(files);
		parse(files);
		parseBatch(files);
	}

	private static ArrayList<File> getPaths(String[] roots) {
		ArrayList<File> files = new ArrayList<>();
		for (String root : roots)
			files.addAll(getPaths(new File(root)));
		return files;
	}

	private static ArrayList<File> getPaths(File dir) {
		ArrayList<File> files = new ArrayList<>();
		if (dir.isDirectory())
			for (File sub : dir.listFiles())
				files.addAll(getPaths(sub));
		else if (dir.getName().endsWith(".java"))
			files.add(dir);
		return files;
	}

	private static void parseBatch(ArrayList<File> files) {
		long start = System.currentTimeMillis();
		mismatches = 0;
		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
		HashMap<String, CompilationUnit> cus = new HashMap<>();
		FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				cus.put(sourceFilePath, ast);
			}
		};
		init();
		parser.createASTs(paths, null, new String[0], r, null);
		for (String path : cus.keySet()) {
			ASTNode ast = asts.get(path);
			ASTNode node = cus.get(path);
			if (!ast.subtreeMatch(new ASTMatcher() {
				@Override
				public boolean match(MethodInvocation node, Object other) {
					if (super.match(node, other)) {
						if (node.resolveMethodBinding() == null)
							return false;
						MethodInvocation mi = (MethodInvocation) other;
						if (node.resolveMethodBinding().isEqualTo(mi.resolveMethodBinding()))
							return true;
						return false;
					}
					return false;
				}
			}, node)) {
				mismatches++;
				//throw new RuntimeException();
				System.err.println("Not matched!!! " + mismatches);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Batch: " + (end - start));
	}

	public static void parse(ArrayList<File> files) {
		long start = System.currentTimeMillis();
		mismatches = 0;
		for (File file : files) {
			String name = file.getName();
			String source = FileIO.readStringFromFile(file.getAbsolutePath());
			init();
			parser.setSource(source.toCharArray());
			parser.setUnitName(name);
			ASTNode node = parser.createAST(null);
			ASTNode ast = asts.get(file.getAbsolutePath());
			if (ast == null)
				asts.put(file.getAbsolutePath(), node);
			else
				if (!ast.subtreeMatch(new ASTMatcher() {
					@Override
					public boolean match(MethodInvocation node, Object other) {
						if (super.match(node, other)) {
							if (node.resolveMethodBinding() == null)
								return false;
							MethodInvocation mi = (MethodInvocation) other;
							if (node.resolveMethodBinding().isEqualTo(mi.resolveMethodBinding()))
								return true;
							return false;
						}
						return false;
					}
				}, node)) {
					mismatches++;
					//throw new RuntimeException();
					System.err.println("Not matched!!! " + mismatches);
				}
		}
		long end = System.currentTimeMillis();
		System.out.println("Online: " + (end - start));
	}

	public static void init() {
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(
				new String[]{"D:/Projects/aug-miner.jar"}, 
				new String[]{}, 
				new String[]{}, 
				true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
	}

}
