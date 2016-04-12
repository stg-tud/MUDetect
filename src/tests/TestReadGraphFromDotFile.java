package tests;

import java.io.File;

import groum.GROUMGraph;

public class TestReadGraphFromDotFile {

	public static void main(String[] args) {
		GROUMGraph g = new GROUMGraph(new File("D:/Projects/GROUMiner/output/patterns/aspectj-1.5-1459981352/4/2_21/256499.dot"));
		g.setName("groum");
		g.toGraphics("D:/temp");
	}

}
