package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.mining.MinerTestUtils.mineWithMinSupport;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsForClasses;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsFromFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinerTest {

	@Test
	public void mineOrderedNodesOfSameReceiver() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_mine_ordered_nodes_of_same_receiver.java");
		
		List<APIUsagePattern> patterns = mineWithMinSupport(groums, 10);
		
//		assertThat(patterns.size(), is(1));
		print(patterns);
	}
	
	@Test
	public void mineExceptionNodes() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_try.java");
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineKeepDataNodes() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_keep_data.java");
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineSinglePattern() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_mine_single.java");
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void mineMinimalCode() {
		Collection<APIUsageExample> groums = buildAUGsForClasses(new String[]{
				"class C { void m(Object o) { o.hashCode(); } }",
				"class C { void m(Object o) { o.hashCode(); } }"});
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineLargerCode() {
		Collection<APIUsageExample> groums = buildAUGsForClasses(new String[]{
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }",
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }"});
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineClone() {
		Collection<APIUsageExample> groums = buildAUGsForClasses(new String[]{
				"class C { void m(Object o, Object p) {  o = getObj(); o.hashCode(); o.hashCode();} }",
				"class C { void m(Object o, Object p) {  o.hashCode();} }"});
		System.out.println(groums);
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void minePattern_aclang2() {
		Collection<APIUsageExample> groums = buildAUGsForClasses(new String[]{
				"class NullTextNull extends StrBuilder {\n" + 
				"  String pattern(Object obj) {\n" + 
				"    String str = (obj == null ? this.getNullText() : obj.toString());\n" + 
				"    if (str == null) {\n" + 
				"      str = \"\";\n" + 
				"    }\n" + 
				"    return str;\n" + 
				"  }\n" + 
				"}",
				"class NullTextNull extends StrBuilder {\n" + 
				"  String pattern(Object obj) {\n" + 
				"    String str = (obj == null ? this.getNullText() : obj.toString());\n" + 
				"    if (str == null) {\n" + 
				"      str = \"\";\n" + 
				"    }\n" + 
				"    return str;\n" + 
				"  }\n" + 
				"}"}
		);
		
		System.out.println(groums);
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		
		print(patterns);
	}
	
	@Test
	public void decryptPattern() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_alibaba2_new.java");
		groums.addAll(buildAUGsFromFile("input/Test_alibaba2_new.java"));
		System.out.println(groums);
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(1));
		assertThat(patterns.get(0).vertexSet().size(), is(groums.iterator().next().getNodeSize()));
	}
	
	@Test
	public void decryptTarget() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_alibaba2_old.java");
		groums.addAll(buildAUGsFromFile("input/Test_alibaba2_old.java"));
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(1));
		assertThat(patterns.get(0).vertexSet().size(), not(groums.iterator().next().getNodeSize()));
	}
	
	@Test
	public void decrypt() {
		Collection<APIUsageExample> groums = buildAUGsFromFile("input/Test_alibaba2_old.java");
		groums.addAll(buildAUGsFromFile("input/Test_alibaba2_old.java"));
		
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(1));
		
		boolean contains = false;
		for (APIUsagePattern p : patterns) {
			for (Node node : p.vertexSet())
				if (node.getLabel().equals("Cipher.getInstance()")) {
					contains = true;
					break;
				}
		}
		assertThat(contains, is(true));
	}

	@Ignore
	@Test
	public void jackrabbit1() {
		String targetSource = "public class ItemManager {\n" + 
				"    private SessionImpl session;" + 
				"    private boolean canRead(ItemData data, Path path) throws AccessDeniedException, RepositoryException {\n" + 
				"        if (data.getState().getStatus() == ItemState.STATUS_NEW && !data.getDefinition().isProtected()) {\n" + 
				"            return true;\n" + 
				"        } else {\n" + 
				"            return (path == null) ? canRead(data.getId()) : session.getAccessManager().canRead(path);\n" + 
				"        }\n" + 
				"    }\n" + 
				"  private boolean canRead(ItemId id) { return true; }\n" + 
				"}";
		String patternSource = "class CheckStateNotNull {\n" + 
			"  boolean canRead(ItemData data, Path path, SessionImpl session) throws AccessDeniedException, RepositoryException {\n" + 
			"    ItemState state = data.getState();\n" + 
			"    if (state == null) {\n" + 
			"        throw new InvalidItemStateException(data.getId() + \": the item does not exist anymore\");\n" + 
			"    }\n" + 
			"    if (state.getStatus() == ItemState.STATUS_NEW && !data.getDefinition().isProtected()) {\n" + 
			"        return true;\n" + 
			"    } else {\n" + 
			"        return (path == null) ? canRead(data.getId()) : session.getAccessManager().canRead(path);\n" + 
			"    }\n" + 
			"  }\n" + 
			"  \n" + 
			"  private boolean canRead(ItemId id) { return true; }\n" + 
			"}";
		Collection<APIUsageExample> groums = buildAUGsForClasses(new String[]{targetSource, patternSource});
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(groums);

		print(patterns);
		assertThat(patterns.size(), is(1));
	}

	@Test
	public void acmath_1() {
		String targetSource = "class SubLine {\n" + 
				"    private Line line;" +
				"    public Vector3D intersection(final SubLine subLine, final boolean includeEndPoints) {\n" + 
				"        // compute the intersection on infinite line\n" + 
				"        Vector3D v1D = line.intersection(subLine.line);\n" + 
				"        // check location of point with respect to first sub-line\n" + 
				"        Location loc1 = remainingRegion.checkPoint(line.toSubSpace(v1D));\n" + 
				"        // check location of point with respect to second sub-line\n" + 
				"        Location loc2 = subLine.remainingRegion.checkPoint(subLine.line.toSubSpace(v1D));\n" + 
				"        if (includeEndPoints) {\n" + 
				"            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v1D : null;\n" + 
				"        } else {\n" + 
				"            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v1D : null;\n" + 
				"        }\n" + 
				"    }\n" + 
				"}";
		String patternSource = "class CheckStateNotNull {\n" + 
			"  public Vector3D pattern(Line line, Line other) {\n" + 
			"    Vector3D v1D = line.intersection(other);\n" + 
			"    if (v1D == null) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"    line.toSubSpace(v1D);\n" + 
			"    other.toSubSpace(v1D);\n" + 
			"    return v1D;\n" + 
			"  }\n" + 
			"}";
		List<APIUsagePattern> patterns = MinerTestUtils.mineMethods(new Configuration() {{minPatternSupport = 2; abstractConditionEdges = true;}},targetSource, patternSource);
		
//		assertThat(patterns, hasSize(2));
		print(patterns);
		assertThat(patterns, hasSize(2));
	}

	@Test
	public void mineAlternativeChecks() {
		String firstHasNext = "class C { void m(Collection c) { Iterator i = c.iterator(); if (i.hasNext()) i.next(); } }";
		String firstIsEmpty = "class C { void n(Collection c) { if (!c.isEmpty()) { Iterator i = c.iterator(); i.next(); } } }";
		Collection<APIUsageExample> augs = buildAUGsForClasses(new String[]{firstHasNext, firstHasNext, firstIsEmpty, firstIsEmpty});
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(augs);

		assertThat(patterns, hasSize(2));
	}

	@Test
	public void repeatedConsecutiveCalls() {
		String example = "class C { void m() {" +
						"  StringBuilder sb = new StringBuilder();" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"}}";
		Collection<APIUsageExample> examples = buildAUGsForClasses(new String[]{example, example});
		List<APIUsagePattern> patterns = MinerTestUtils.mineWithMinSupport2(examples);

		assertThat(patterns, hasSize(1));
		APIUsagePattern pattern = patterns.get(0);
		Collection<Node> patternNodes = pattern.vertexSet();
		long numberOfAppendCalls = patternNodes.stream().filter(node -> node.getLabel().equals("AbstractStringBuilder.append()")).count();
		assertThat(numberOfAppendCalls, is(1L));
	}

	private void print(APIUsageGraph graph) {
		System.out.println(new DisplayAUGDotExporter().toDotGraph(graph));
	}

	private void print(Collection<? extends APIUsageGraph> graphs) {
		for (APIUsageGraph graph : graphs) {
			print(graph);
		}
	}
}
