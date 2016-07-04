/**
 * 
 */
package tests;

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
		for (EGroumGraph groum : gb.build("input/" + "Test.java")) {
			groum.toGraphics("output");
		}
	}
}
