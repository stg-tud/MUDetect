package egroum;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileIO;
import utils.JavaASTUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EGroumTestUtils {
	public static EGroumGraph buildGroumForMethod(String code) {
		String classCode = "class C { " + code + "}";
		ArrayList<EGroumGraph> groums = buildGroumsForClass(classCode);
		assertThat(groums.size(), is(1));
		return groums.iterator().next();
	}

	public static ArrayList<EGroumGraph> buildGroumsForClass(String classCode) {
		EGroumBuilder builder = new EGroumBuilder();
		return builder.buildGroums(classCode, "test", "test");
	}

	public static void buildAndPrintGroumsForFile(String inputPath, String name, String outputPath) {
		EGroumBuilder gb = new EGroumBuilder();
		inputPath = inputPath + "/" + name;
		String content = FileIO.readStringFromFile(inputPath);
		ASTNode ast = JavaASTUtil.parseSource(content, inputPath, name);
		CompilationUnit cu = (CompilationUnit) ast;
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		for (MethodDeclaration m : type.getMethods()) {
			EGroumGraph g = gb.buildGroum(m, inputPath, type.getName().getIdentifier() + ".");
			String s = m.toString();
			s = s.replace("\n", "\\l");
			s = s.replace("\t", "    ");
			s = s.replace("\"", "\\\"");
			s += "\\l";
			s = "0 [label=\"" + s + "\"" + " shape=box style=dotted]";
//			System.out.println(s);
			g.toGraphics(s, outputPath);
		}
	}
}
