package typeresolution;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumBuilder;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mcs.CISGraph;
import mcs.MCISFinder;
import mcs.MCSFragment;
import mining.Lattice;
import mining.Miner;
import mining.Pattern;
import utils.FileIO;

public class BatchParser {
	private static ASTParser parser;
	private static HashMap<String, ASTNode> asts = new HashMap<>();
	private static int mismatches = 0, mismatchesInOnline = 0, mismatchesInBatch = 0;
	
	@Rule
	public TestName testName = new TestName();

	@Test
	public void testReuseParser() {
		ArrayList<File> files = getPaths(new String[] {"src"});
//		String classPath = "D:/Projects/GROUMiner/target/classes";
		String classPath = "D:/Projects/aug-miner.jar";
		parse(files, classPath);
		parse(files, classPath);
		assertThat(mismatches, is(0));
		parseBatch(files, classPath);
		assertThat(mismatches, is(0));
	}

	@Test
	public void testReuseParserJDT() {
		ArrayList<File> files = getPaths(new String[] {"C:/Users/hoan/.m2/repository/org/eclipse/tycho/org.eclipse.jdt.core/3.11.1.v20150902-1521/org.eclipse.jdt.core-3.11.1.v20150902-1521-sources"});
		String classPath = "C:/Users/hoan/.m2/repository/org/eclipse/tycho/org.eclipse.jdt.core/3.11.1.v20150902-1521/org.eclipse.jdt.core-3.11.1.v20150902-1521.jar";
		parse(files, classPath);
		parse(files, classPath);
		assertThat(mismatches, is(0));
		parseBatch(files, classPath);
		assertThat(mismatches, is(0));
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

	private void parseBatch(ArrayList<File> files, String classPath) {
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
		init(classPath);
		parser.createASTs(paths, null, new String[0], r, null);
		for (String path : cus.keySet()) {
			ASTNode ast = asts.get(path);
			ASTNode node = cus.get(path);
			if (!ast.subtreeMatch(new ASTMatcher() {
				@Override
				public boolean match(MethodInvocation node, Object other) {
					if (super.match(node, other)) {
						MethodInvocation mi = (MethodInvocation) other;
						if (node.resolveMethodBinding() == null) {
							if (mi.resolveMethodBinding() == null)
								return true;
							mismatchesInOnline++;
							return false;
						}
						if (mi.resolveMethodBinding() == null) {
							mismatchesInBatch++;
							return false;
						}
						if (node.resolveMethodBinding().isEqualTo(mi.resolveMethodBinding()))
							return true;
						return false;
					}
					return false;
				}
			}, node)) {
				mismatches++;
				//throw new RuntimeException();
//				System.err.println("Not matched!!! " + mismatches + " / " + files.size());
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Batch: " + mismatches + " / " + files.size() + " in " + (end - start) + "ms");
		System.out.println("mismatchesInBatch: " + mismatchesInBatch);
		System.out.println("mismatchesInOnline: " + mismatchesInOnline);
	}

	public void parse(ArrayList<File> files, String classPath) {
		long start = System.currentTimeMillis();
		mismatches = 0;
		for (File file : files) {
			String name = file.getName();
			String source = FileIO.readStringFromFile(file.getAbsolutePath());
			init(classPath);
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
							MethodInvocation mi = (MethodInvocation) other;
							if (node.resolveMethodBinding() == null) {
								if (mi.resolveMethodBinding() == null)
									return true;
								return false;
							}
							if (node.resolveMethodBinding().isEqualTo(mi.resolveMethodBinding()))
								return true;
							return false;
						}
						return false;
					}
				}, node)) {
					mismatches++;
					//throw new RuntimeException();
					System.err.println("Not matched!!! " + mismatches + " / " + files.size());
				}
		}
		long end = System.currentTimeMillis();
		System.out.println("Online: " + mismatches + " / " + files.size() + " in " + (end - start) + "ms");
	}

	public void init(String classPath) {
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(
				new String[]{classPath}, 
				new String[]{}, 
				new String[]{}, 
				true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
	}
}
