package egroum;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

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
		EGroumBuilder builder = new EGroumBuilder("");
		builder.buildGroums("class C { " + code + "}", "test");
		List<EGroumGraph> groums = builder.getGroums();
		assertThat(groums.size(), is(1));
		return groums.get(0);
	}

	private void print(EGroumGraph groum) {
		DotGraph dotGraph = new DotGraph(groum);
		System.out.println(dotGraph.getGraph());
		dotGraph.toPNG(new File("output"), name.getMethodName());
	}
}
