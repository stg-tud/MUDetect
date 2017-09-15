package egroum;

import graphics.DotGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;

import static egroum.EGroumTestUtils.buildGroumForMethod;

public class EGroumBuilderTest {
	@Rule public TestName name = new TestName();
	
	@Test
	public void reassignment() {
		print(buildGroumForMethod("void m(int i) { if (i < 0) { i = -i; m(i); } }"));
	}
	
	@Test
	public void alternativeReassignments() {
		print(buildGroumForMethod("void m(int i) { if (i < 0)	i = -i;	else i++; m(i); }"));
	}
	
	@Test
	public void controlChars() {
		print(buildGroumForMethod("String cc() { return \" \\n \\t \\b \\f \\\\ \\\" \"; }"));
	}
	
	@Test
	public void controlInstructions() {
		print(buildGroumForMethod("void m() {"
				+ " while(true) {"
				+ "  if (1 == 2) continue;"
				+ "  else break; this.m();"
				+ "  super.m();"
				+ "  assert true;"
				+ "  throw new RuntimeException(); }"));
	}

	@Test
	public void encodesExceptionHandling() throws Exception {
		print(buildGroumForMethod("void m() {" +
				"  try {\n" +
				"      throw new FileNotFoundException();\n" +
				"    } catch(FileNotFoundException fnfe) {\n" +
				"      fnfe.printStackTrace();\n" +
				"    }" +
				"}"));
	}

    private void print(EGroumGraph groum) {
		DotGraph dotGraph = new DotGraph(groum);
		System.out.println(dotGraph.getGraph());
		dotGraph.toPNG(new File("output"), name.getMethodName());
	}
}
