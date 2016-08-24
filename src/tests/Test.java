/**
 * 
 */
package tests;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import egroum.EGroumBuilder;
import egroum.EGroumGraph;

/**
 * @author hoan
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EGroumBuilder gb = new EGroumBuilder();
		for (EGroumGraph groum : gb.build("input/" + "Test3.java")) {
//		for (EGroumGraph groum : gb.build("T:\\repos\\lucene-solr")) {
			groum.toGraphics("T:/output");
		}
	}
}
