/**
 * 
 */
package groum;

/**
 * @author hoan
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GROUMBuilder gb = new GROUMBuilder("input/" + "TestGrum3.java");
		gb.build();
		for (GROUMGraph groum : gb.getGroums())
		{
			groum.toGraphics("output");
		}
	}
}
