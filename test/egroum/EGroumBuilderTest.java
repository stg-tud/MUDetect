package egroum;

import graphics.DotGraph;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import tests.GroumValidationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

import static egroum.EGroumTestUtils.buildGroumForMethod;

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
	public void forSimon() {
		print(buildGroumForMethod("public int fileInputStreamExample(String filePath) throws FileNotFoundException, IOException {\n" + 
				"		File file = new File(filePath);\n" + 
				"		FileInputStream fis = new FileInputStream(file);\n" + 
				"		\n" + 
				"		int firstByte = fis.read();\n" + 
				"		fis.close();\n" + 
				"		\n" + 
				"		return firstByte;\n" + 
				"	}"));
	}
	
	@Test @Ignore
	public void illegalOutNode() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("/Users/svenamann/Documents/PhD/API Misuse Benchmark/MUBench/checkouts/itext/5091/original-src/com/itextpdf/text");
		ArrayList<EGroumGraph> groums = new EGroumBuilder(new String[]{}).build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			GroumValidationUtils.validate(groum);
		}
	}
	
	@Test @Ignore
	public void illegalOutNode1() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\itext\\5090\\original-src");
		ArrayList<EGroumGraph> groums = new EGroumBuilder(new String[]{}).build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			GroumValidationUtils.validate(groum);
		}
	}
	
	@Test @Ignore
	public void illegalOutNode2() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\itext\\5091\\original-src");
		ArrayList<EGroumGraph> groums = new EGroumBuilder(new String[]{}).build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			GroumValidationUtils.validate(groum);
		}
	}
	
	@Test @Ignore
	public void illegalOutNode3() throws IOException {
		FileSystem FileSystem = FileSystems.getDefault();
		Path targetSourcePath = FileSystem.getPath("T:\\repos\\lucene-solr");
		ArrayList<EGroumGraph> groums = new EGroumBuilder(new String[]{}).build(targetSourcePath.toString());
		
		for (EGroumGraph groum : groums) {
			GroumValidationUtils.validate(groum);
		}
	}

	@Test
	public void endlessGroum() {
		EGroumGraph groum = buildGroumForMethod("public boolean equals(Object obj) {\n" + 
				"        if (obj == this) {\n" + 
				"            return true;\n" + 
				"        }\n" + 
				"        if (!(obj instanceof CategoryLineAnnotation)) {\n" + 
				"            return false;\n" + 
				"        }\n" + 
				"        CategoryLineAnnotation that = (CategoryLineAnnotation) obj;\n" + 
				"        if (!this.category1.equals(that.getCategory1())) {\n" + 
				"            return false;\n" + 
				"        }\n" + 
				"        if (this.value1 != that.getValue1()) {\n" + 
				"            return false;    \n" + 
				"        }\n" + 
				"        if (!this.category2.equals(that.getCategory2())) {\n" + 
				"            return false;\n" + 
				"        }\n" + 
				"        if (this.value2 != that.getValue2()) {\n" + 
				"            return false;    \n" + 
				"        }\n" + 
				"        if (!PaintUtilities.equal(this.paint, that.paint)) {\n" + 
				"            return false;\n" + 
				"        }\n" + 
				"        if (!ObjectUtilities.equal(this.stroke, that.stroke)) {\n" + 
				"            return false;\n" + 
				"        }\n" + 
				"        return true;\n" + 
				"    }");
		
		GroumValidationUtils.validate(groum);
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

    @Test
    public void encodesFinally() throws Exception {
        print(buildGroumForMethod("void m() throws Exception {\n" +
                "  try {\n" +
                "    throw new Exception();\n" +
                "  } finally {\n" +
                "    m();\n" +
                "  }\n" +
                "}"));
    }

    private void print(EGroumGraph groum) {
		DotGraph dotGraph = new DotGraph(groum);
		System.out.println(dotGraph.getGraph());
		dotGraph.toPNG(new File("output"), name.getMethodName());
	}
}
