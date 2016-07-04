package egroum;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import graphics.DotGraph;

public class EGroumBuilderTest {
	@Rule public TestName name = new TestName();

	@Test
	public void singleCall() {
		print(buildGroumForMethod("void m(Object o) { o.hashCode(); }"));
	}
	
	@Test
	public void singleCallAssignResult() {
		print(buildGroumForMethod("void m(Object o) { int i = o.hashCode(); }"));
	}
	
	@Test
	public void reassignment() {
		print(buildGroumForMethod("void m(int i) { if (i < 0) { i = -i; m(i); } }"));
	}
	
	@Test
	public void alternativeReassignments() {
		print(buildGroumForMethod("void m(int i) { if (i < 0)	i = -i;	else i++; m(i); }"));
	}

	private EGroumGraph buildGroumForMethod(String code) {
		EGroumBuilder builder = new EGroumBuilder();
		ArrayList<EGroumGraph> groums = builder.buildGroums("class C { " + code + "}", "test");
		assertThat(groums.size(), is(1));
		return groums.iterator().next();
	}

	private void print(EGroumGraph groum) {
		DotGraph dotGraph = new DotGraph(groum);
		System.out.println(dotGraph.getGraph());
		dotGraph.toPNG(new File("output"), name.getMethodName());
	}
}
