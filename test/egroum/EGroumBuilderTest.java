package egroum;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

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
	public void illegalOutNode() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("/Users/svenamann/Documents/PhD/API Misuse Benchmark/MUBench/checkouts/itext/5091/original-src/com/itextpdf/text");
		ArrayList<EGroumGraph> groums = new EGroumBuilder().build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			HashSet<EGroumNode> nodes = groum.getNodes();
			for (EGroumNode node : nodes) {
				for (EGroumNode outNode : node.getOutNodes()) {
					assertThat(nodes, hasItem(outNode));
				}
			}
		}
	}
	
	@Test
	public void illegalOutNode1() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\itext\\5090\\original-src");
		ArrayList<EGroumGraph> groums = new EGroumBuilder().build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			HashSet<EGroumNode> nodes = groum.getNodes();
			for (EGroumNode node : nodes) {
				for (EGroumNode outNode : node.getOutNodes()) {
					assertThat(nodes, hasItem(outNode));
				}
			}
		}
	}
	
	@Test
	public void illegalOutNode2() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\itext\\5091\\original-src");
		ArrayList<EGroumGraph> groums = new EGroumBuilder().build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			HashSet<EGroumNode> nodes = groum.getNodes();
			for (EGroumNode node : nodes) {
				for (EGroumNode outNode : node.getOutNodes()) {
					assertThat(nodes, hasItem(outNode));
				}
			}
		}
	}
	
	@Test
	public void illegalOutNode3() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\lucene-solr");
		ArrayList<EGroumGraph> groums = new EGroumBuilder().build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			HashSet<EGroumNode> nodes = groum.getNodes();
			for (EGroumNode node : nodes) {
				for (EGroumNode outNode : node.getOutNodes()) {
					assertThat(nodes, hasItem(outNode));
				}
			}
		}
	}

	private EGroumGraph buildGroumForMethod(String code) {
		String classCode = "class C { " + code + "}";
		ArrayList<EGroumGraph> groums = buildGroumsForClass(classCode);
		assertThat(groums.size(), is(1));
		return groums.iterator().next();
	}

	private ArrayList<EGroumGraph> buildGroumsForClass(String classCode) {
		EGroumBuilder builder = new EGroumBuilder();
		return builder.buildGroums(classCode, "test");
	}

	private void print(EGroumGraph groum) {
		DotGraph dotGraph = new DotGraph(groum);
		System.out.println(dotGraph.getGraph());
		dotGraph.toPNG(new File("output"), name.getMethodName());
	}
}
