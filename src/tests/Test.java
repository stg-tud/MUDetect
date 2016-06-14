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
		EGroumBuilder gb = new EGroumBuilder("input/" + "Test.java");
		gb.build();
		for (EGroumGraph groum : gb.getGroums()) {
			groum.toGraphics("output");
		}
	}
}
