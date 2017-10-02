package mining;

import egroum.EGroumGraph;
import egroum.EGroumNode;
import egroum.EGroumTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.List;

import static egroum.EGroumTestUtils.buildGroumsForClasses;
import static mining.MinerTestUtils.mineWithMinSupport2;
import static mining.MinerTestUtils.mineWithMinSupport;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinerTest {
	
	@Rule
	public TestName testName = new TestName();
	
	@Test
	public void mineOrderedNodesOfSameReceiver() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_mine_ordered_nodes_of_same_receiver.java");
		
		List<Pattern> patterns = mineWithMinSupport(groums, 10);
		
//		assertThat(patterns.size(), is(1));
		print(patterns);
	}
	
	@Test
	public void mineExceptionNodes() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_try.java");
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineKeepDataNodes() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_keep_data.java");
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineSinglePattern() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_mine_single.java");
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void mineMinimalCode() {
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{
				"class C { void m(Object o) { o.hashCode(); } }",
				"class C { void m(Object o) { o.hashCode(); } }"});
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineLargerCode() {
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }",
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }"});
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineClone() {
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{
				"class C { void m(Object o, Object p) {  o = getObj(); o.hashCode(); o.hashCode();} }",
				"class C { void m(Object o, Object p) {  o.hashCode();} }"});
		System.out.println(groums);
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void minePattern_aclang2() {
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{
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
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns);
	}
	
	@Test
	public void decryptPattern() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_new.java");
		groums.addAll(EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_new.java"));
		System.out.println(groums);
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(1));
		assertThat(patterns.get(0).getRepresentative().getNodes().size(), is(groums.get(0).getNodes().size()));
	}
	
	@Test
	public void decryptTarget() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_old.java");
		groums.addAll(EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_old.java"));
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(2));
		assertThat(patterns.get(0).getRepresentative().getNodes().size(), not(groums.get(0).getNodes().size()));
	}
	
	@Test
	public void decrypt() {
		ArrayList<EGroumGraph> groums = EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_old.java");
		groums.addAll(EGroumTestUtils.buildGroumsFromFile("test-resources/input/Test_alibaba2_old.java"));
		
		List<Pattern> patterns = mineWithMinSupport2(groums);
		print(patterns);
		
		assertThat(patterns.size(), is(2));
		
		boolean contains = false;
		for (Pattern p : patterns) {
			for (EGroumNode node : p.getRepresentative().getNodes())
				if (node.getLabel().equals("Cipher.getInstance()")) {
					contains = true;
					break;
				}
		}
		assertThat(contains, is(true));
	}
	
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
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{targetSource, patternSource});
		for (EGroumGraph g : groums)
			g.toGraphics("temp");
		
		List<Pattern> patterns = mineWithMinSupport2(groums);

		print(patterns);
		assertThat(patterns.size(), is(2)); // DEBUG
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
		ArrayList<EGroumGraph> groums = buildGroumsForClasses(new String[]{targetSource, patternSource});
		List<Pattern> patterns = mineWithMinSupport2(groums);
		
		print(patterns);
		assertThat(patterns, hasSize(2));
	}

	@Test
	public void mineAlternativeChecks() throws Exception {
		String firstHasNext = "class C { void m(Collection c) { Iterator i = c.iterator(); if (i.hasNext()) i.next(); } }";
		String firstIsEmpty = "class C { void n(Collection c) { if (!c.isEmpty()) { Iterator i = c.iterator(); i.next(); } } }";
		ArrayList<EGroumGraph> augs = buildGroumsForClasses(new String[]{firstHasNext, firstHasNext, firstIsEmpty, firstIsEmpty});
		List<Pattern> patterns = mineWithMinSupport2(augs);

		assertThat(patterns, hasSize(2));
	}

	@Test
	public void mineAlternativeCond() throws Exception {
		String iteratorIf = "class C { void m(Iterator i) { if (i.hasNext()) { i.next(); } } }";
		String iteratorWhile = "class C { void n(Iterator i) { while (i.hasNext()) { i.next(); } } }";
		ArrayList<EGroumGraph> augs = buildGroumsForClasses(new String[]{iteratorIf, iteratorIf, iteratorIf, iteratorWhile, iteratorWhile});
		for (EGroumGraph aug : augs)
			System.out.println(aug);
		List<Pattern> patterns = mineWithMinSupport2(augs);
		
		System.out.println(patterns.size());
		for (Pattern p : patterns)
			System.out.println(p.getRepresentative().getNodes());
		assertThat(patterns, hasSize(2));
	}

	@Test
	public void repeatedConsecutiveCalls() throws Exception {
		String example = "class C { void m() {" +
						"  StringBuilder sb = new StringBuilder();" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"  sb.append(\"1\");" +
						"}}";
		ArrayList<EGroumGraph> examples = buildGroumsForClasses(new String[]{example, example});
		List<Pattern> patterns = mineWithMinSupport2(examples);

		assertThat(patterns, hasSize(1));
		Pattern pattern = patterns.get(0);
		ArrayList<EGroumNode> patternNodes = pattern.getRepresentative().getNodes();
		long numberOfAppendCalls = patternNodes.stream().filter(node -> node.getLabel().equals("AbstractStringBuilder.append()")).count();
		assertThat(numberOfAppendCalls, is(1L));
	}

	private void print(Pattern pattern) {
		MinerTestUtils.print(pattern, testName);
	}

	private void print(List<Pattern> patterns) {
		MinerTestUtils.print(patterns, testName);
	}
}
