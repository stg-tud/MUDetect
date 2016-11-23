package mcisg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import mining.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumBuilder;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mcs.CISGraph;
import mcs.MCISFinder;
import mcs.MCSFragment;
import mining.Lattice;
import mining.Miner;
import mining.Pattern;
import utils.FileIO;

public class MCISGFinderTest {

	
	@Rule
	public TestName testName = new TestName();

	@Test
	public void mineDuplicatedCode() {
		String inputPath = "test-resources/input", system = "anonymous_class2";
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			groums.addAll(buildGroums(FileIO.readStringFromFile(inputPath + "/Test_" + system + "_pattern.java")));
		
		if (groums.size() <= 2)
			for (EGroumGraph g : groums){
				System.out.println(g);
				g.toGraphics("T:/temp");
			}
		
		List<Pattern> patterns = mine(groums);
		
		for (EGroumGraph g : groums) {
			System.out.println(g);
			g.toGraphics("T:/temp");
		}
		print(patterns);
		assertThat(patterns.size(), is(1));
		
		groums = buildGroums(FileIO.readStringFromFile(inputPath + "/Test_" + system + "_target.java"));
		EGroumGraph pattern = new EGroumGraph(patterns.get(0).getRepresentative()).collapse();
		groums.add(pattern);
		
		
		for (EGroumGraph g : groums) {
			System.out.println(g);
			g.toGraphics("T:/temp");
		}
		
		HashSet<EGroumNode> patternNodes = new HashSet<>(pattern.getNodes());
		HashSet<EGroumEdge> patternEdges = new HashSet<>();
		for (EGroumNode node : patternNodes) {
			patternEdges.addAll(node.getInEdges());
			patternEdges.addAll(node.getOutEdges());
		}
		
		ArrayList<CISGraph> cigs = match(new HashSet<>(groums));
		
		print(cigs);
		assertThat(cigs.size(), is(1));
		
		for (CISGraph cig: cigs)
			printMissing(cig, pattern, patternEdges);
	}

	private void printMissing(CISGraph cig, EGroumGraph pattern, HashSet<EGroumEdge> patternEdges) {
		MCSFragment f = null;
		boolean hasOther = false;
		for (MCSFragment t : cig.getFragments()) {
			if (t.getGraph() == pattern) {
				f = t;
			} else 
				hasOther = true;
		}
		if (f == null || !hasOther)
			return;
		HashSet<EGroumNode> nodes = new HashSet<>(pattern.getNodes());
		nodes.removeAll(f.getNodes());
		HashSet<EGroumEdge> edges = new HashSet<>(patternEdges);
		edges.removeAll(f.getEdges());
		if (!edges.isEmpty()) {
			System.out.println("Missing nodes:");
			print(nodes);
			System.out.println("Missing edges:");
			print(edges);
			pattern.toGraphics("T:/temp", nodes, edges);
		}
	}

	private <E> void print(HashSet<E> edges) {
		for (E e : edges) {
			System.out.println(e);
		}
	}
	
	@SuppressWarnings("unused")
	private ArrayList<EGroumGraph> buildGroumsFromFile(String path) {
		return new EGroumBuilder(new String[]{}).build(path);
	}

	private ArrayList<EGroumGraph> buildGroums(String... sourceCodes) {
		EGroumBuilder builder = new EGroumBuilder(new String[]{});
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.addAll(builder.buildGroums(sourceCode, "", ""));
		}
		return groums;
	}

	private List<Pattern> mine(ArrayList<EGroumGraph> groums) {
		Miner miner = new Miner("test", new Configuration() {{ minPatternSupport = 2; maxPatternSize = 30; }});
		return new ArrayList<>(miner.mine(groums));
	}

	private ArrayList<CISGraph> match(HashSet<EGroumGraph> groums) {
		MCISFinder matcher = new MCISFinder();
		return matcher.match(groums);
	}
	
	@SuppressWarnings("unused")
	private void print(Pattern pattern) {
		print(Arrays.asList(pattern));
	}
	
	private void print(List<Pattern> patterns) {
		System.err.println("Test '" + testName.getMethodName() + "':");
		for (Pattern pattern : patterns) {
			HashSet<EGroumNode> set = new HashSet<>(pattern.getRepresentative().getNodes());
			assertThat(set.size(), is(pattern.getRepresentative().getNodes().size()));
			EGroumGraph g = new EGroumGraph(pattern.getRepresentative());
			System.err.println(" - " + g);
			g.toGraphics("T:/temp");
		}
	}

	private void print(ArrayList<CISGraph> cigs) {
		System.err.println("Test '" + testName.getMethodName() + "':");
		for (CISGraph pattern : cigs) {
			HashSet<EGroumNode> set = new HashSet<>(pattern.getRepresentative().getNodes());
			assertThat(set.size(), is(pattern.getRepresentative().getNodes().size()));
			EGroumGraph g = new EGroumGraph(pattern.getRepresentative());
			System.err.println(" - " + g);
			g.toGraphics("T:/temp");
		}
	}
}
